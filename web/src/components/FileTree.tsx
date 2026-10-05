import { useState } from 'react'
import { type Adapters, type BrickId, BRICKS, isBrickActive } from '../project/bricks'
import { type TreeNode, isModified } from '../project/sources'

interface Props {
  nodes: TreeNode[]
  activeFile: string
  /** The brick highlighted in the diagram: the dots of its files light up. */
  activeBrick: BrickId
  adapters: Adapters
  onOpen: (path: string) => void
}

export function FileTree({ nodes, activeFile, activeBrick, adapters, onOpen }: Props) {
  const [collapsed, setCollapsed] = useState<Set<string>>(() => new Set())

  const toggle = (path: string) =>
    setCollapsed((previous) => {
      const next = new Set(previous)
      if (next.has(path)) next.delete(path)
      else next.add(path)
      return next
    })

  const render = (node: TreeNode, depth: number) => {
    const indent = { paddingLeft: `${8 + depth * 10}px` }

    if (node.file) {
      const file = node.file
      const inactive = !isBrickActive(file.brick, adapters)
      return (
        <li key={node.path}>
          <button
            type="button"
            className={`tree-row tree-file${file.path === activeFile ? ' active' : ''}${inactive ? ' inactive' : ''}`}
            style={indent}
            onClick={() => onOpen(file.path)}
            aria-current={file.path === activeFile ? 'true' : undefined}
          >
            <i className={`codicon ${iconOf(file.name)}`} aria-hidden="true" />
            <span className="tree-name">{node.name}</span>
            <span
              className={`brick-dot brick-${file.brick}${file.brick === activeBrick ? ' lit' : ''}`}
              title={BRICKS[file.brick].label}
              aria-hidden="true"
            />
            {isModified(file, adapters) && <span className="tree-badge" title="Changed by the adapter swap">M</span>}
          </button>
        </li>
      )
    }

    const open = !collapsed.has(node.path)
    return (
      <li key={node.path}>
        <button
          type="button"
          className="tree-row tree-folder"
          style={indent}
          onClick={() => toggle(node.path)}
          aria-expanded={open}
        >
          <i className={`codicon codicon-chevron-${open ? 'down' : 'right'}`} aria-hidden="true" />
          <span className="tree-name" title={node.name}>{node.name}</span>
        </button>
        {open && <ul>{node.children.map((child) => render(child, depth + 1))}</ul>}
      </li>
    )
  }

  return <ul className="tree">{nodes.map((node) => render(node, 0))}</ul>
}

function iconOf(name: string): string {
  if (name.endsWith('.java')) return 'codicon-symbol-class'
  if (name.endsWith('.xml')) return 'codicon-code'
  if (name.endsWith('.yml') || name.endsWith('.yaml')) return 'codicon-settings-gear'
  if (name.endsWith('.sql')) return 'codicon-database'
  if (name.endsWith('.json')) return 'codicon-json'
  return 'codicon-file'
}
