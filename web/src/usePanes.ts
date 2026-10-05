import { useCallback, useEffect, useState } from 'react'

export type PaneId = 'tree' | 'side'

export interface Pane {
  width: number
  collapsed: boolean
}

export type Panes = Record<PaneId, Pane>

/** Width limits of each side pane, in CSS pixels. */
export const PANE_LIMITS: Record<PaneId, { min: number; max: number }> = {
  tree: { min: 180, max: 480 },
  side: { min: 340, max: 760 },
}

/** The code always keeps at least this much room. */
export const MIN_CODE_WIDTH = 360

const STORAGE_KEY = 'hexagonal-101.panes'

function defaults(): Panes {
  const side = Math.round(Math.min(560, Math.max(400, window.innerWidth * 0.34)))
  return { tree: { width: 280, collapsed: false }, side: { width: side, collapsed: false } }
}

function load(): Panes {
  const fallback = defaults()
  try {
    const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? 'null') as Partial<Panes> | null
    if (!stored) return fallback
    const pane = (id: PaneId): Pane => {
      const value = stored[id]
      if (!value || typeof value.width !== 'number') return fallback[id]
      const { min, max } = PANE_LIMITS[id]
      return { width: Math.min(max, Math.max(min, value.width)), collapsed: Boolean(value.collapsed) }
    }
    return { tree: pane('tree'), side: pane('side') }
  } catch {
    return fallback
  }
}

/** Widths and collapsed state of the explorer and the diagram, remembered in the browser. */
export function usePanes() {
  const [panes, setPanes] = useState<Panes>(load)

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(panes))
    } catch {
      // Storage unavailable (private mode, blocked site data): the layout simply is not remembered.
    }
  }, [panes])

  const update = useCallback(
    (id: PaneId, change: Partial<Pane>) => setPanes((current) => ({ ...current, [id]: { ...current[id], ...change } })),
    [],
  )

  const toggle = useCallback(
    (id: PaneId) => setPanes((current) => ({ ...current, [id]: { ...current[id], collapsed: !current[id].collapsed } })),
    [],
  )

  return { panes, update, toggle }
}

/** True below the width where the layout stacks vertically and panes can no longer be resized. */
export function useNarrowScreen(): boolean {
  const query = '(max-width: 900px)'
  const [narrow, setNarrow] = useState(() => window.matchMedia(query).matches)
  useEffect(() => {
    const media = window.matchMedia(query)
    const onChange = (event: MediaQueryListEvent) => setNarrow(event.matches)
    media.addEventListener('change', onChange)
    return () => media.removeEventListener('change', onChange)
  }, [])
  return narrow
}
