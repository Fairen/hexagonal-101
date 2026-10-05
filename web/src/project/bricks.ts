export type InboundAdapter = 'rest' | 'kafka'
export type OutboundAdapter = 's3' | 'sqlite'

export interface Adapters {
  inbound: InboundAdapter
  outbound: OutboundAdapter
}

/** The defaults, as written in application.yml on disk. */
export const DEFAULT_ADAPTERS: Adapters = { inbound: 'rest', outbound: 's3' }

export type BrickId =
  | 'domain'
  | 'port-in'
  | 'use-case'
  | 'port-out'
  | 'adapter-rest'
  | 'adapter-kafka'
  | 'adapter-s3'
  | 'adapter-sqlite'
  | 'bootstrap'

export interface Brick {
  id: BrickId
  label: string
  /** The file opened when the brick is clicked in the diagram. */
  mainFile: string
}

/** Root of the Java sources: one package per brick below it. */
export const JAVA = 'src/main/java/dev/hexagonal101/library'
const APP = `${JAVA}/application`

export const BRICKS: Record<BrickId, Brick> = {
  domain: {
    id: 'domain',
    label: 'Domain',
    mainFile: `${JAVA}/domain/LoanPolicy.java`,
  },
  'port-in': {
    id: 'port-in',
    label: 'Driving port',
    mainFile: `${APP}/port/in/BorrowBookUseCase.java`,
  },
  'use-case': {
    id: 'use-case',
    label: 'Use case',
    mainFile: `${APP}/service/BorrowBookService.java`,
  },
  'port-out': {
    id: 'port-out',
    label: 'Driven ports',
    mainFile: `${APP}/port/out/LoanRepository.java`,
  },
  'adapter-rest': {
    id: 'adapter-rest',
    label: 'REST',
    mainFile: `${JAVA}/adapter/in/rest/LoanController.java`,
  },
  'adapter-kafka': {
    id: 'adapter-kafka',
    label: 'Kafka',
    mainFile: `${JAVA}/adapter/in/kafka/LoanRequestListener.java`,
  },
  'adapter-s3': {
    id: 'adapter-s3',
    label: 'S3 · JSON',
    mainFile: `${JAVA}/adapter/out/s3/S3LoanRepository.java`,
  },
  'adapter-sqlite': {
    id: 'adapter-sqlite',
    label: 'SQLite',
    mainFile: `${JAVA}/adapter/out/sqlite/SqliteLoanRepository.java`,
  },
  bootstrap: {
    id: 'bootstrap',
    label: 'Bootstrap',
    mainFile: 'src/main/resources/application.yml',
  },
}

export const ADAPTER_BRICK = {
  rest: 'adapter-rest',
  kafka: 'adapter-kafka',
  s3: 'adapter-s3',
  sqlite: 'adapter-sqlite',
} as const satisfies Record<InboundAdapter | OutboundAdapter, BrickId>

/** False only for the adapter bricks that are not plugged in. */
export function isBrickActive(brick: BrickId, adapters: Adapters): boolean {
  switch (brick) {
    case 'adapter-rest':
    case 'adapter-kafka':
      return ADAPTER_BRICK[adapters.inbound] === brick
    case 'adapter-s3':
    case 'adapter-sqlite':
      return ADAPTER_BRICK[adapters.outbound] === brick
    default:
      return true
  }
}

/** Which brick a file belongs to: its package, or for resources the adapter that uses them. */
export function brickOfPath(path: string): BrickId {
  if (path.startsWith(`${JAVA}/domain/`)) return 'domain'
  if (path.startsWith(`${APP}/port/in/`)) return 'port-in'
  if (path.startsWith(`${APP}/port/out/`)) return 'port-out'
  if (path.startsWith(`${APP}/`)) return 'use-case'
  if (path.startsWith(`${JAVA}/adapter/in/rest/`)) return 'adapter-rest'
  if (path.startsWith(`${JAVA}/adapter/in/kafka/`)) return 'adapter-kafka'
  if (path.startsWith(`${JAVA}/adapter/out/s3/`) || path.startsWith('src/main/resources/s3/')) return 'adapter-s3'
  if (path.startsWith(`${JAVA}/adapter/out/sqlite/`) || path.startsWith('src/main/resources/db/sqlite/')) {
    return 'adapter-sqlite'
  }
  return 'bootstrap'
}
