import { type Adapters, BRICKS, DEFAULT_ADAPTERS, type InboundAdapter, type OutboundAdapter } from './project/bricks'
import { APPLICATION_YML } from './project/sources'
import { loanFlow } from './content/flow'

export interface State {
  adapters: Adapters
  activeFile: string
  /** Index of the current step of the request animation, or null when it is not running. */
  flowIndex: number | null
  playing: boolean
  /** Incremented on every adapter swap, to replay the "changed lines" flash. */
  swaps: number
}

export type Action =
  | { type: 'open'; path: string }
  | { type: 'plug'; adapter: InboundAdapter | OutboundAdapter }
  | { type: 'flow-start' }
  | { type: 'flow-goto'; index: number }
  | { type: 'flow-play'; playing: boolean }
  | { type: 'flow-stop' }

export const initialState: State = {
  adapters: DEFAULT_ADAPTERS,
  activeFile: BRICKS.domain.mainFile,
  flowIndex: null,
  playing: false,
  swaps: 0,
}

export function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'open':
      return { ...state, activeFile: action.path, flowIndex: null, playing: false }

    case 'plug': {
      const adapters: Adapters =
        action.adapter === 'rest' || action.adapter === 'kafka'
          ? { ...state.adapters, inbound: action.adapter }
          : { ...state.adapters, outbound: action.adapter }
      if (adapters.inbound === state.adapters.inbound && adapters.outbound === state.adapters.outbound) {
        return state
      }
      // During the animation, stay on the same step of the (re-computed) journey.
      // Otherwise, show application.yml: the only file that changes.
      const activeFile =
        state.flowIndex === null ? APPLICATION_YML : loanFlow(adapters)[state.flowIndex].file
      return { ...state, adapters, activeFile, swaps: state.swaps + 1 }
    }

    case 'flow-start': {
      const steps = loanFlow(state.adapters)
      return { ...state, flowIndex: 0, playing: true, activeFile: steps[0].file }
    }

    case 'flow-goto': {
      const steps = loanFlow(state.adapters)
      const index = Math.max(0, Math.min(action.index, steps.length - 1))
      return { ...state, flowIndex: index, activeFile: steps[index].file }
    }

    case 'flow-play':
      return { ...state, playing: action.playing }

    case 'flow-stop':
      return { ...state, flowIndex: null, playing: false }
  }
}
