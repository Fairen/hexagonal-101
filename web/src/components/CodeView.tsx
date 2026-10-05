import { useEffect, useRef, useState } from 'react'
import Editor from '@monaco-editor/react'
import type { monaco } from '../project/monaco'
import type { ProjectFile } from '../project/sources'

interface Props {
  file: ProjectFile
  content: string
  keyLines: string[]
  focusLine?: string
  modifiedLines: number[]
  swaps: number
}

type CodeEditor = monaco.editor.IStandaloneCodeEditor

export function CodeView({ file, content, keyLines, focusLine, modifiedLines, swaps }: Props) {
  const [editor, setEditor] = useState<CodeEditor | null>(null)
  const decorations = useRef<monaco.editor.IEditorDecorationsCollection | null>(null)

  // Arrays are compared by value: re-decorating (and re-scrolling) only when something really changed.
  const keyLinesKey = keyLines.join('\n')
  const modifiedKey = modifiedLines.join(',')

  // Key lines, the current step of the animation, and lines changed by an adapter swap.
  useEffect(() => {
    if (!editor) return
    const model = editor.getModel()
    if (!model) return
    decorations.current ??= editor.createDecorationsCollection()

    const lines = model.getLinesContent()
    const lineNumbersMatching = (pattern: string) =>
      lines.flatMap((line, index) => (line.includes(pattern) ? [index + 1] : []))

    const key = new Set(keyLinesKey ? keyLinesKey.split('\n').flatMap(lineNumbersMatching) : [])
    const modified = modifiedKey ? modifiedKey.split(',').map(Number) : []
    const focus = focusLine ? lineNumbersMatching(focusLine)[0] : undefined

    const whole = (line: number) => ({ startLineNumber: line, startColumn: 1, endLineNumber: line, endColumn: 1 })
    decorations.current.set([
      ...[...key].map((line) => ({
        range: whole(line),
        options: { isWholeLine: true, className: 'line-key', linesDecorationsClassName: 'gutter-key' },
      })),
      ...modified.map((line) => ({
        range: whole(line),
        options: {
          isWholeLine: true,
          // Alternating the class name restarts the CSS flash animation on every swap.
          className: `line-modified flash-${swaps % 2}`,
          linesDecorationsClassName: 'gutter-modified',
        },
      })),
      ...(focus
        ? [{ range: whole(focus), options: { isWholeLine: true, className: 'line-focus', linesDecorationsClassName: 'gutter-focus' } }]
        : []),
    ])

    const reveal = focus ?? modified[0]
    if (reveal) editor.revealLineInCenter(reveal)
  }, [editor, file.path, content, keyLinesKey, focusLine, modifiedKey, swaps])

  return (
    <div className="editor">
      <Editor
        path={file.path}
        value={content}
        language={file.language}
        theme="hexagonal-dark"
        onMount={(mounted) => setEditor(mounted)}
        options={{
          readOnly: true,
          domReadOnly: true,
          minimap: { enabled: false },
          scrollBeyondLastLine: false,
          fontSize: 13,
          lineNumbersMinChars: 3,
          renderLineHighlight: 'none',
          automaticLayout: true,
          padding: { top: 10 },
          stickyScroll: { enabled: false },
        }}
      />
    </div>
  )
}
