import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The site reads the real Java sources from ../java at build time.
const repoRoot = fileURLToPath(new URL('..', import.meta.url))

export default defineConfig({
  // Root by default (Vercel, local preview); the GitHub Pages workflow sets BASE_PATH=/<repo>/.
  base: process.env.BASE_PATH ?? '/',
  plugins: [react()],
  server: {
    fs: { allow: [repoRoot] },
  },
  build: {
    // Monaco alone is ~3.5 MB minified (~0.9 MB gzipped); there is nothing smaller to split it into.
    chunkSizeWarningLimit: 4000,
  },
})
