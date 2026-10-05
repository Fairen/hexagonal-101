import { BRICK_INFO, fileInfo } from '../content/annotations'
import type { ProjectFile } from '../project/sources'

export function ExplanationPanel({ file }: { file: ProjectFile }) {
  const brick = BRICK_INFO[file.brick]
  const info = fileInfo(file.path)
  return (
    <section className="explanation" aria-live="polite">
      <div className="explanation-head">
        <span className={`brick-dot brick-${file.brick}`} aria-hidden="true" />
        <h2>{brick.title}</h2>
        <span className="explanation-role">{brick.role}</span>
      </div>
      <p>{brick.text}</p>
      {info && (
        <>
          <h3>{file.name}</h3>
          <p>{info.text}</p>
        </>
      )}
    </section>
  )
}
