/**
 * The animated logo: the architecture in one loop.
 * <p>
 * The domain core breathes inside the application ring. On each side an adapter plugs into its port,
 * unplugs, and a different one takes its place (circle ↔ square: REST ↔ Kafka, S3 ↔ SQLite).
 * Whenever both sides are plugged, a request crosses the hexagon and the core lights up as it passes.
 * The timings live in styles.css (.logo-*).
 */
export function Logo() {
  return (
    <svg className="logo" viewBox="0 0 64 64" role="img" aria-label="Hexagonal 101">
      {/* Wires between the ports and the adapters */}
      <line className="logo-wire" x1="9" y1="32" x2="14.7" y2="32" />
      <line className="logo-wire" x1="49.3" y1="32" x2="55" y2="32" />

      {/* Application ring, drawn once on load */}
      <polygon className="logo-ring" points="32,12 49.3,22 49.3,42 32,52 14.7,42 14.7,22" pathLength={120} />

      {/* Domain core: breathes, flashes when a request reaches it */}
      <g className="logo-core">
        <polygon points="32,23 39.8,27.5 39.8,36.5 32,41 24.2,36.5 24.2,27.5" />
        <polygon className="logo-core-inner" points="32,28 35.5,30 35.5,34 32,36 28.5,34 28.5,30" />
      </g>

      {/* Ports on the left and right edges */}
      <rect className="logo-port" x="12.7" y="28" width="4" height="8" rx="1.2" />
      <rect className="logo-port" x="47.3" y="28" width="4" height="8" rx="1.2" />

      {/* Swappable adapters */}
      <circle className="logo-plug logo-plug-in logo-plug-a" cx="7" cy="32" r="3.6" />
      <rect className="logo-plug logo-plug-in logo-plug-b" x="3.4" y="28.4" width="7.2" height="7.2" rx="1.6" />
      <circle className="logo-plug logo-plug-out logo-plug-a" cx="57" cy="32" r="3.6" />
      <rect className="logo-plug logo-plug-out logo-plug-b" x="53.4" y="28.4" width="7.2" height="7.2" rx="1.6" />

      {/* A request travelling through the hexagon */}
      <circle className="logo-request" cx="0" cy="32" r="2.2" />
    </svg>
  )
}
