import type { Dispatch } from 'react'
import type { Action } from '../state'
import type { FlowStep } from '../content/flow'

interface Props {
  step: FlowStep | null
  index: number | null
  total: number
  playing: boolean
  dispatch: Dispatch<Action>
}

export function FlowControls({ step, index, total, playing, dispatch }: Props) {
  if (step === null || index === null) {
    return (
      <button type="button" className="flow-start" onClick={() => dispatch({ type: 'flow-start' })}>
        <i className="codicon codicon-play" aria-hidden="true" /> Follow a loan request through the hexagon
      </button>
    )
  }

  return (
    <section className="flow" aria-live="polite">
      <div className="flow-head">
        <span className="flow-count">
          Step {index + 1} / {total}
        </span>
        <div className="flow-buttons">
          <button type="button" aria-label="Previous step" disabled={index === 0}
            onClick={() => dispatch({ type: 'flow-goto', index: index - 1 })}>
            <i className="codicon codicon-chevron-left" aria-hidden="true" />
          </button>
          <button type="button" aria-label={playing ? 'Pause' : 'Play'}
            onClick={() => dispatch({ type: 'flow-play', playing: !playing })}>
            <i className={`codicon codicon-debug-${playing ? 'pause' : 'start'}`} aria-hidden="true" />
          </button>
          <button type="button" aria-label="Next step" disabled={index === total - 1}
            onClick={() => dispatch({ type: 'flow-goto', index: index + 1 })}>
            <i className="codicon codicon-chevron-right" aria-hidden="true" />
          </button>
          <button type="button" aria-label="Stop" onClick={() => dispatch({ type: 'flow-stop' })}>
            <i className="codicon codicon-close" aria-hidden="true" />
          </button>
        </div>
      </div>
      <p className="flow-caption">{step.caption}</p>
      <div className="flow-progress" aria-hidden="true">
        <div style={{ width: `${((index + 1) / total) * 100}%` }} />
      </div>
    </section>
  )
}
