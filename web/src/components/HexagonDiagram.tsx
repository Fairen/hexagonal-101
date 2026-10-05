import type { CSSProperties, KeyboardEvent, ReactNode } from 'react'
import {
  type Adapters,
  type BrickId,
  type InboundAdapter,
  type OutboundAdapter,
  ADAPTER_BRICK,
  isBrickActive,
} from '../project/bricks'

interface Props {
  adapters: Adapters
  activeBrick: BrickId
  /** Brick of the previous animation step: a dot travels from there to the active brick. */
  flowFrom?: BrickId
  flowKey: number | null
  onOpenBrick: (brick: BrickId) => void
  onPlug: (adapter: InboundAdapter | OutboundAdapter) => void
}

// Geometry of the drawing (SVG user units).
const W = 640
const H = 430
const CX = 320
const CY = 200
const OUTER_R = 150
const INNER_R = 70
const CARD_W = 116
const CARD_H = 72
const LEFT_X = 10
const RIGHT_X = W - 10 - CARD_W
const TOP_Y = 110
const BOTTOM_Y = 214

const PORT_IN = { x: 150, y: 162, w: 102, h: 76 }
const PORT_OUT = { x: 388, y: 162, w: 102, h: 76 }
const USE_CASE = { x: 246, y: 90, w: 148, h: 30 }
const BOOTSTRAP = { x: CX - 120, y: 380, w: 240, h: 26 }

const CENTERS: Record<BrickId, [number, number]> = {
  domain: [CX, CY],
  'port-in': [PORT_IN.x + PORT_IN.w / 2, CY],
  'use-case': [CX, USE_CASE.y + USE_CASE.h / 2],
  'port-out': [PORT_OUT.x + PORT_OUT.w / 2, CY],
  'adapter-rest': [LEFT_X + CARD_W / 2, TOP_Y + CARD_H / 2],
  'adapter-kafka': [LEFT_X + CARD_W / 2, BOTTOM_Y + CARD_H / 2],
  'adapter-s3': [RIGHT_X + CARD_W / 2, TOP_Y + CARD_H / 2],
  'adapter-sqlite': [RIGHT_X + CARD_W / 2, BOTTOM_Y + CARD_H / 2],
  bootstrap: [CX, BOOTSTRAP.y + BOOTSTRAP.h / 2],
}

function hexagon(radius: number): string {
  return Array.from({ length: 6 }, (_, k) => {
    const angle = ((-90 + 60 * k) * Math.PI) / 180
    return `${(CX + radius * Math.cos(angle)).toFixed(1)},${(CY + radius * Math.sin(angle)).toFixed(1)}`
  }).join(' ')
}

interface AdapterCard {
  adapter: InboundAdapter | OutboundAdapter
  title: string
  subtitle: string
  x: number
  y: number
  side: 'in' | 'out'
}

const CARDS: AdapterCard[] = [
  { adapter: 'rest', title: 'REST', subtitle: 'POST /loans', x: LEFT_X, y: TOP_Y, side: 'in' },
  { adapter: 'kafka', title: 'Kafka', subtitle: 'loan-requests', x: LEFT_X, y: BOTTOM_Y, side: 'in' },
  { adapter: 's3', title: 'S3', subtitle: 'library.json', x: RIGHT_X, y: TOP_Y, side: 'out' },
  { adapter: 'sqlite', title: 'SQLite', subtitle: 'JdbcClient', x: RIGHT_X, y: BOTTOM_Y, side: 'out' },
]

export function HexagonDiagram({ adapters, activeBrick, flowFrom, flowKey, onOpenBrick, onPlug }: Props) {
  const brickClass = (brick: BrickId) =>
    `brick brick-${brick}${brick === activeBrick ? ' is-active' : ''}${isBrickActive(brick, adapters) ? '' : ' is-unplugged'}`

  const clickable = (brick: BrickId, label: string) => ({
    role: 'button',
    tabIndex: 0,
    'aria-label': label,
    onClick: () => onOpenBrick(brick),
    onKeyDown: (event: KeyboardEvent) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault()
        onOpenBrick(brick)
      }
    },
  })

  const from = flowFrom && flowFrom !== activeBrick ? CENTERS[flowFrom] : undefined
  const to = CENTERS[activeBrick]

  return (
    <figure className="diagram">
      <svg viewBox={`0 0 ${W} ${H}`} role="group" aria-label="Hexagonal architecture of the library">
        {/* Bootstrap: the frame that assembles everything */}
        <rect className="bootstrap-frame" x={4} y={4} width={W - 8} height={H - 8} rx={14} />
        <g className={brickClass('bootstrap')} {...clickable('bootstrap', 'Bootstrap: open application.yml')}>
          <rect x={BOOTSTRAP.x} y={BOOTSTRAP.y} width={BOOTSTRAP.w} height={BOOTSTRAP.h} rx={13} />
          <text x={CX} y={BOOTSTRAP.y + 17} className="label-strong">
            bootstrap · application.yml
          </text>
        </g>

        <text x={LEFT_X + CARD_W / 2} y={TOP_Y - 14} className="side-label">driving adapters</text>
        <text x={RIGHT_X + CARD_W / 2} y={TOP_Y - 14} className="side-label">driven adapters</text>

        {/* Wires between adapters and ports */}
        {CARDS.map((card) => {
          const plugged = isBrickActive(ADAPTER_BRICK[card.adapter], adapters)
          const cy = card.y + CARD_H / 2
          const d =
            card.side === 'in'
              ? `M${card.x + CARD_W},${cy} C${card.x + CARD_W + 20},${cy} ${PORT_IN.x - 20},${CY} ${PORT_IN.x},${CY}`
              : `M${PORT_OUT.x + PORT_OUT.w},${CY} C${PORT_OUT.x + PORT_OUT.w + 20},${CY} ${card.x - 20},${cy} ${card.x},${cy}`
          return <path key={card.adapter} d={d} className={plugged ? 'wire is-plugged' : 'wire'} />
        })}

        {/* Application: the ring around the domain */}
        <g className={brickClass('use-case')} {...clickable('use-case', 'Application: open BorrowBookService')}>
          <polygon points={hexagon(OUTER_R)} className="ring" />
          <text x={CX} y={CY + 108} className="ring-label">application</text>
        </g>

        <g className={brickClass('use-case')} {...clickable('use-case', 'Use case: open BorrowBookService')}>
          <rect x={USE_CASE.x} y={USE_CASE.y} width={USE_CASE.w} height={USE_CASE.h} rx={14} />
          <text x={CX} y={USE_CASE.y + 19.5} className="label-strong">BorrowBookService</text>
        </g>

        {/* Domain: the core */}
        <g className={brickClass('domain')} {...clickable('domain', 'Domain: open LoanPolicy')}>
          <polygon points={hexagon(INNER_R)} />
          <text x={CX} y={CY - 6} className="label-title">domain</text>
          <text x={CX} y={CY + 12} className="label-small">LoanPolicy</text>
          <text x={CX} y={CY + 27} className="label-small">Loan · Member…</text>
        </g>

        {/* Ports: plugs on the edges of the hexagon */}
        <g className={brickClass('port-in')} {...clickable('port-in', 'Driving port: open BorrowBookUseCase')}>
          <rect x={PORT_IN.x} y={PORT_IN.y} width={PORT_IN.w} height={PORT_IN.h} rx={8} />
          <PortText x={PORT_IN.x + PORT_IN.w / 2} title="port in" lines={['BorrowBook', 'UseCase']} />
        </g>
        <g className={brickClass('port-out')} {...clickable('port-out', 'Driven ports: open LoanRepository')}>
          <rect x={PORT_OUT.x} y={PORT_OUT.y} width={PORT_OUT.w} height={PORT_OUT.h} rx={8} />
          <PortText x={PORT_OUT.x + PORT_OUT.w / 2} title="ports out" lines={['BookCatalog', 'MemberDirectory', 'LoanRepository']} />
        </g>

        {/* Adapters: the swappable plugs */}
        {CARDS.map((card) => {
          const brick = ADAPTER_BRICK[card.adapter]
          const plugged = isBrickActive(brick, adapters)
          return (
            <g key={card.adapter} className={brickClass(brick)} {...clickable(brick, `${card.title} adapter: open its code`)}>
              <rect x={card.x} y={card.y} width={CARD_W} height={CARD_H} rx={10} />
              <text x={card.x + CARD_W / 2} y={card.y + 23} className="label-title">{card.title}</text>
              <text x={card.x + CARD_W / 2} y={card.y + 39} className="label-small">{card.subtitle}</text>
              {plugged ? (
                <text x={card.x + CARD_W / 2} y={card.y + 59} className="status-plugged">● plugged in</text>
              ) : (
                <PlugButton x={card.x + CARD_W / 2} y={card.y + 47} label={`Plug in ${card.title}`}
                  onPlug={() => onPlug(card.adapter)} />
              )}
            </g>
          )
        })}

        {/* The travelling dot of the request animation */}
        {flowKey !== null && from && (
          <g key={flowKey} aria-hidden="true">
            <line x1={from[0]} y1={from[1]} x2={to[0]} y2={to[1]} className="flow-trail" />
            <circle
              r={7}
              className="flow-dot"
              style={{ '--from-x': `${from[0]}px`, '--from-y': `${from[1]}px`, '--to-x': `${to[0]}px`, '--to-y': `${to[1]}px` } as CSSProperties}
            />
          </g>
        )}
      </svg>
      <figcaption>Click a brick to open its code. Click “plug in” to swap an adapter.</figcaption>
    </figure>
  )
}

function PortText({ x, title, lines }: { x: number; title: string; lines: string[] }): ReactNode {
  const top = CY - ((lines.length + 1) * 14) / 2 + 10
  return (
    <>
      <text x={x} y={top} className="label-caps">{title}</text>
      {lines.map((line, index) => (
        <text key={line} x={x} y={top + 15 + index * 14} className="label-small">{line}</text>
      ))}
    </>
  )
}

function PlugButton({ x, y, label, onPlug }: { x: number; y: number; label: string; onPlug: () => void }) {
  return (
    <g
      className="plug-button"
      role="button"
      tabIndex={0}
      aria-label={label}
      onClick={(event) => {
        event.stopPropagation()
        onPlug()
      }}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault()
          event.stopPropagation()
          onPlug()
        }
      }}
    >
      <rect x={x - 40} y={y} width={80} height={20} rx={10} />
      <text x={x} y={y + 14}>⇄ plug in</text>
    </g>
  )
}
