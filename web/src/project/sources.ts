import { type Adapters, type BrickId, brickOfPath, DEFAULT_ADAPTERS } from './bricks'

// The real Java project, read at build time. Tests and build output are left out on purpose.
const raw = import.meta.glob<string>(
  [
    '../../../java/pom.xml',
    '../../../java/compose.yaml',
    '../../../java/src/main/**/*.{java,yml,yaml,sql,json}',
  ],
  { query: '?raw', import: 'default', eager: true },
)

export type Language = 'java' | 'xml' | 'yaml' | 'sql' | 'javascript' | 'plaintext'

export interface ProjectFile {
  /** Path relative to the Maven project root, e.g. "src/main/java/.../domain/LoanPolicy.java". */
  path: string
  name: string
  brick: BrickId
  language: Language
  content: string
}

function languageOf(name: string): Language {
  const extension = name.slice(name.lastIndexOf('.') + 1)
  switch (extension) {
    case 'java':
      return 'java'
    case 'xml':
      return 'xml'
    case 'yml':
    case 'yaml':
      return 'yaml'
    case 'sql':
      return 'sql'
    case 'json':
      return 'javascript'
    default:
      return 'plaintext'
  }
}

export const FILES: ProjectFile[] = Object.entries(raw)
  .map(([globPath, content]) => {
    const path = globPath.replace('../../../java/', '')
    const name = path.slice(path.lastIndexOf('/') + 1)
    return { path, name, brick: brickOfPath(path), language: languageOf(name), content }
  })
  .sort((a, b) => a.path.localeCompare(b.path))

export const FILES_BY_PATH = new Map(FILES.map((file) => [file.path, file]))

export const APPLICATION_YML = 'src/main/resources/application.yml'

/**
 * The content to display. Every file is shown as it is on disk, except application.yml,
 * whose two adapter properties follow what the user plugged in the diagram.
 */
export function displayedContent(file: ProjectFile, adapters: Adapters): string {
  if (file.path !== APPLICATION_YML) return file.content
  return file.content
    .replace(/(\binbound:\s+)(\w+)(\s+)#/, (_, key: string, value: string, gap: string) =>
      key + adapters.inbound.padEnd(value.length + gap.length) + '#',
    )
    .replace(/(\boutbound:\s+)(\w+)(\s+)#/, (_, key: string, value: string, gap: string) =>
      key + adapters.outbound.padEnd(value.length + gap.length) + '#',
    )
}

/** Line numbers (1-based) whose displayed content differs from the file on disk. */
export function modifiedLines(file: ProjectFile, adapters: Adapters): number[] {
  const original = file.content.split('\n')
  return displayedContent(file, adapters)
    .split('\n')
    .flatMap((line, index) => (line === original[index] ? [] : [index + 1]))
}

export function isModified(file: ProjectFile, adapters: Adapters): boolean {
  return (
    file.path === APPLICATION_YML &&
    (adapters.inbound !== DEFAULT_ADAPTERS.inbound || adapters.outbound !== DEFAULT_ADAPTERS.outbound)
  )
}

export interface TreeNode {
  /** Display name; compacted folders read "src/main/java/dev/…". */
  name: string
  /** Unique key: the folder or file path. */
  path: string
  children: TreeNode[]
  file?: ProjectFile
}

/** Builds the explorer tree, with single-child folders compacted like VS Code does. */
export function buildTree(files: ProjectFile[]): TreeNode[] {
  const root: TreeNode = { name: '', path: '', children: [] }
  for (const file of files) {
    const segments = file.path.split('/')
    let node = root
    segments.forEach((segment, index) => {
      const path = segments.slice(0, index + 1).join('/')
      let child = node.children.find((candidate) => candidate.path === path)
      if (!child) {
        child = { name: segment, path, children: [] }
        if (index === segments.length - 1) child.file = file
        node.children.push(child)
      }
      node = child
    })
  }

  // Folders first, then files, each in alphabetical order: the classic explorer order.
  const byExplorerOrder = (a: TreeNode, b: TreeNode) =>
    Number(Boolean(a.file)) - Number(Boolean(b.file)) ||
    a.name.localeCompare(b.name, 'en', { sensitivity: 'base' })

  const compact = (node: TreeNode): TreeNode => {
    let current = node
    while (!current.file && current.children.length === 1 && !current.children[0].file) {
      const only = current.children[0]
      current = { ...only, name: `${current.name}/${only.name}` }
    }
    return { ...current, children: current.children.map(compact).sort(byExplorerOrder) }
  }

  return root.children.map(compact).sort(byExplorerOrder)
}
