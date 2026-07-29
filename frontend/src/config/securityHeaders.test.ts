import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const here = dirname(fileURLToPath(import.meta.url))
const confPath = resolve(here, '../../security-headers.conf')

describe('security-headers.conf', () => {
  const conf = readFileSync(confPath, 'utf-8')
  const cspLine = conf.split('\n').find((line) => line.includes('Content-Security-Policy'))

  it('defines a Content-Security-Policy header', () => {
    expect(cspLine).toBeDefined()
  })

  it('does not contain unsafe-inline (SEC-14, #110)', () => {
    expect(cspLine).not.toContain('unsafe-inline')
  })

  it('still restricts style-src and script-src to self', () => {
    expect(cspLine).toContain("script-src 'self'")
    expect(cspLine).toContain("style-src 'self'")
  })
})
