import { type KeyboardEvent, type PointerEvent, useRef, useState } from 'react'
import { type Pane, type PaneId, MIN_CODE_WIDTH, PANE_LIMITS } from '../usePanes'

interface Props {
  pane: PaneId
  /** Which edge of the code the pane sits on: dragging towards the code makes the pane wider. */
  edge: 'left' | 'right'
  state: Pane
  /** Width of the other side pane (0 when collapsed), so the code keeps its minimum room. */
  otherWidth: number
  label: string
  controls: string
  onChange: (change: Partial<Pane>) => void
  onDraggingChange: (dragging: boolean) => void
}

const KEYBOARD_STEP = 24

/**
 * A vertical separator between two panes.
 * Drag to resize, drag a pane below half its minimum width to collapse it, drag back out to restore it.
 * Double-click or Enter toggles the pane; arrow keys resize it.
 */
export function Splitter({ pane, edge, state, otherWidth, label, controls, onChange, onDraggingChange }: Props) {
  const drag = useRef<{ startX: number; startWidth: number } | null>(null)
  const [dragging, setDragging] = useState(false)
  const { min, max } = PANE_LIMITS[pane]

  const maxWidth = () => Math.max(min, Math.min(max, window.innerWidth - otherWidth - MIN_CODE_WIDTH))
  const clamp = (width: number) => Math.round(Math.min(maxWidth(), Math.max(min, width)))

  const onPointerDown = (event: PointerEvent<HTMLDivElement>) => {
    if (event.button !== 0) return
    event.preventDefault()
    event.currentTarget.setPointerCapture(event.pointerId)
    drag.current = { startX: event.clientX, startWidth: state.collapsed ? 0 : state.width }
    setDragging(true)
    onDraggingChange(true)
  }

  const onPointerMove = (event: PointerEvent<HTMLDivElement>) => {
    if (!drag.current) return
    const delta = event.clientX - drag.current.startX
    const { startWidth } = drag.current
    const raw = startWidth + (edge === 'left' ? delta : -delta)
    if (raw < min / 2) {
      // Collapsing: keep the width the pane had before the drag, to restore it later.
      onChange(startWidth > 0 ? { collapsed: true, width: startWidth } : { collapsed: true })
    } else {
      onChange({ collapsed: false, width: clamp(raw) })
    }
  }

  const endDrag = () => {
    if (!drag.current) return
    drag.current = null
    setDragging(false)
    onDraggingChange(false)
  }

  const onKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    const grow = edge === 'left' ? 'ArrowRight' : 'ArrowLeft'
    const shrink = edge === 'left' ? 'ArrowLeft' : 'ArrowRight'
    if (event.key === 'Enter' || event.key === ' ') {
      onChange({ collapsed: !state.collapsed })
    } else if (event.key === grow) {
      onChange({ collapsed: false, width: clamp((state.collapsed ? min : state.width) + KEYBOARD_STEP) })
    } else if (event.key === shrink && !state.collapsed) {
      onChange({ width: clamp(state.width - KEYBOARD_STEP) })
    } else {
      return
    }
    event.preventDefault()
  }

  return (
    <div
      role="separator"
      aria-orientation="vertical"
      aria-label={label}
      aria-controls={controls}
      aria-valuemin={0}
      aria-valuemax={max}
      aria-valuenow={state.collapsed ? 0 : state.width}
      tabIndex={0}
      title={`${label} · double-click to ${state.collapsed ? 'show' : 'hide'}`}
      className={`splitter${dragging ? ' dragging' : ''}`}
      onPointerDown={onPointerDown}
      onPointerMove={onPointerMove}
      onPointerUp={endDrag}
      onPointerCancel={endDrag}
      onDoubleClick={() => onChange({ collapsed: !state.collapsed })}
      onKeyDown={onKeyDown}
    />
  )
}
