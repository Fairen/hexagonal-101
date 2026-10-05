// A trimmed-down Monaco: the editor core, a few reading-friendly features, and only the languages we show.
import * as monaco from 'monaco-editor/editor/editor.api'
import 'monaco-editor/editor/contrib/bracketMatching/browser/bracketMatching'
import 'monaco-editor/editor/contrib/clipboard/browser/clipboard'
import 'monaco-editor/editor/contrib/folding/browser/folding'
import 'monaco-editor/editor/contrib/hover/browser/hoverContribution'
import 'monaco-editor/editor/contrib/wordHighlighter/browser/wordHighlighter'
import 'monaco-editor/features/find/register'
// The package exports map only exposes .js files, so the icon font CSS is imported by path.
import '../../node_modules/monaco-editor/esm/vs/base/browser/ui/codicons/codicon/codicon.css'
import 'monaco-editor/languages/definitions/java/register'
import 'monaco-editor/languages/definitions/javascript/register'
import 'monaco-editor/languages/definitions/sql/register'
import 'monaco-editor/languages/definitions/xml/register'
import 'monaco-editor/languages/definitions/yaml/register'
import EditorWorker from 'monaco-editor/editor/editor.worker?worker'
import { loader } from '@monaco-editor/react'

self.MonacoEnvironment = {
  getWorker: () => new EditorWorker(),
}

loader.config({ monaco })

// Matches the site's dark theme (styles.css): same background, muted syntax colours.
monaco.editor.defineTheme('hexagonal-dark', {
  base: 'vs-dark',
  inherit: true,
  rules: [
    { token: 'comment', foreground: '5f6673', fontStyle: 'italic' },
    { token: 'keyword', foreground: '9aa8c7' },
    { token: 'string', foreground: 'a3bf94' },
    { token: 'number', foreground: 'd1a25c' },
    { token: 'annotation', foreground: 'c48aa4' },
    { token: 'type', foreground: 'c9ced6' },
    { token: 'type.identifier', foreground: 'c9ced6' },
    { token: 'tag', foreground: '9aa8c7' },
    { token: 'attribute.name', foreground: '6db3a6' },
    { token: 'attribute.value', foreground: 'a3bf94' },
    { token: 'delimiter', foreground: '848b97' },
  ],
  colors: {
    'editor.background': '#13161b',
    'editor.foreground': '#d9dde4',
    'editorGutter.background': '#13161b',
    'editorLineNumber.foreground': '#454b55',
    'editorLineNumber.activeForeground': '#848b97',
    'editor.lineHighlightBackground': '#00000000',
    'editor.selectionBackground': '#9aa8c733',
    'editorIndentGuide.background1': '#1f232a',
    'editorBracketMatch.background': '#9aa8c722',
    'editorBracketMatch.border': '#9aa8c755',
    'scrollbarSlider.background': '#ffffff12',
    'scrollbarSlider.hoverBackground': '#ffffff1f',
    'scrollbarSlider.activeBackground': '#ffffff2a',
  },
})

export { monaco }
