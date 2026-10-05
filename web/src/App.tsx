import { type CSSProperties, useEffect, useReducer, useState } from 'react'
import { initialState, reducer } from './state'
import { FILES, FILES_BY_PATH, buildTree, displayedContent, isModified, modifiedLines } from './project/sources'
import { ADAPTER_BRICK, BRICKS } from './project/bricks'
import { loanFlow } from './content/flow'
import { FileTree } from './components/FileTree'
import { CodeView } from './components/CodeView'
import { HexagonDiagram } from './components/HexagonDiagram'
import { FlowControls } from './components/FlowControls'
import { ExplanationPanel } from './components/ExplanationPanel'
import { fileInfo } from './content/annotations'
import { HelpButton } from './components/HelpDialog'
import { Logo } from './components/Logo'
import { Splitter } from './components/Splitter'
import { type PaneId, useNarrowScreen, usePanes } from './usePanes'

const TREE = buildTree(FILES)
const STEP_DURATION_MS = 4500

export function App() {
  const [state, dispatch] = useReducer(reducer, initialState)
  const { adapters, activeFile, flowIndex, playing } = state
  const { panes, update, toggle } = usePanes()
  const narrow = useNarrowScreen()
  const [resizing, setResizing] = useState(false)

  // On a narrow screen the panes are stacked: nothing to resize or collapse.
  const collapsed = (id: PaneId) => !narrow && panes[id].collapsed
  const widthOf = (id: PaneId) => (collapsed(id) ? 0 : panes[id].width)

  const file = FILES_BY_PATH.get(activeFile) ?? FILES[0]
  const steps = loanFlow(adapters)
  const step = flowIndex === null ? null : steps[flowIndex]
  const previousStep = flowIndex === null || flowIndex === 0 ? null : steps[flowIndex - 1]

  useEffect(() => {
    if (!playing || flowIndex === null) return
    const timer = window.setTimeout(() => {
      if (flowIndex < steps.length - 1) dispatch({ type: 'flow-goto', index: flowIndex + 1 })
      else dispatch({ type: 'flow-play', playing: false })
    }, STEP_DURATION_MS)
    return () => window.clearTimeout(timer)
  }, [playing, flowIndex, steps.length])

  return (
    <div className="app">
      <header className="header">
        <div className="brand">
          <Logo />
          <h1 className="visually-hidden">Hexagonal 101</h1>
        </div>
        <div className="header-actions">
          {!narrow && (
            <>
              <button
                type="button"
                className="icon-button"
                aria-label="Toggle the explorer"
                title="Toggle the explorer"
                aria-pressed={!panes.tree.collapsed}
                aria-controls="tree-pane"
                onClick={() => toggle('tree')}
              >
                <i className={`codicon codicon-layout-sidebar-left${panes.tree.collapsed ? '-off' : ''}`} aria-hidden="true" />
              </button>
              <button
                type="button"
                className="icon-button"
                aria-label="Toggle the diagram"
                title="Toggle the diagram"
                aria-pressed={!panes.side.collapsed}
                aria-controls="side-pane"
                onClick={() => toggle('side')}
              >
                <i className={`codicon codicon-layout-sidebar-right${panes.side.collapsed ? '-off' : ''}`} aria-hidden="true" />
              </button>
            </>
          )}
          <HelpButton />
        </div>
      </header>

      <main
        className={`layout${resizing ? ' resizing' : ''}`}
        style={{ '--tree-width': `${widthOf('tree')}px`, '--side-width': `${widthOf('side')}px` } as CSSProperties}
      >
        <nav id="tree-pane" className="pane tree-pane" aria-label="Project files" inert={collapsed('tree')}>
          <div className="pane-title">Explorer · library</div>
          <FileTree
            nodes={TREE}
            activeFile={file.path}
            activeBrick={file.brick}
            adapters={adapters}
            onOpen={(path) => dispatch({ type: 'open', path })}
          />
        </nav>

        {!narrow && (
          <Splitter
            pane="tree"
            edge="left"
            state={panes.tree}
            otherWidth={widthOf('side')}
            label="Resize the explorer"
            controls="tree-pane"
            onChange={(change) => update('tree', change)}
            onDraggingChange={setResizing}
          />
        )}

        <section className="pane code-pane" aria-label="Source code">
          <div className="breadcrumb">
            <span className={`brick-dot brick-${file.brick}`} aria-hidden="true" />
            <span className="breadcrumb-path">{file.path}</span>
            {isModified(file, adapters) && <span className="tag tag-modified">modified</span>}
          </div>
          <CodeView
            file={file}
            content={displayedContent(file, adapters)}
            keyLines={fileInfo(file.path)?.keyLines ?? []}
            focusLine={step?.file === file.path ? step.line : undefined}
            modifiedLines={modifiedLines(file, adapters)}
            swaps={state.swaps}
          />
        </section>

        {!narrow && (
          <Splitter
            pane="side"
            edge="right"
            state={panes.side}
            otherWidth={widthOf('tree')}
            label="Resize the diagram"
            controls="side-pane"
            onChange={(change) => update('side', change)}
            onDraggingChange={setResizing}
          />
        )}

        <aside id="side-pane" className="pane side-pane" aria-label="Architecture" inert={collapsed('side')}>
          <HexagonDiagram
            adapters={adapters}
            activeBrick={file.brick}
            flowFrom={previousStep ? FILES_BY_PATH.get(previousStep.file)?.brick : undefined}
            flowKey={flowIndex}
            onOpenBrick={(brick) => dispatch({ type: 'open', path: BRICKS[brick].mainFile })}
            onPlug={(adapter) => dispatch({ type: 'plug', adapter })}
          />
          <FlowControls
            step={step}
            index={flowIndex}
            total={steps.length}
            playing={playing}
            dispatch={dispatch}
          />
          <ExplanationPanel file={file} />
          <p className="plugged">
            Plugged in: <strong>{BRICKS[ADAPTER_BRICK[adapters.inbound]].label}</strong> →
            hexagon → <strong>{BRICKS[ADAPTER_BRICK[adapters.outbound]].label}</strong>
          </p>
        </aside>
      </main>
    </div>
  )
}
