// eslint-plugin-react's latest release (7.37.5) declares a peer range of
// eslint@^3..^9.7 — it does not yet list eslint@^10 (this project's version)
// as supported, even though it works fine in practice for the one rule used
// here. `npm install` in this directory therefore requires --legacy-peer-deps
// (or --force) until upstream publishes ESLint 10 support; a plain
// `npm install`/`npm ci` will fail with an ERESOLVE error otherwise. Revisit
// this note once https://github.com/jsx-eslint/eslint-plugin-react releases
// a version with eslint@^10 in its peerDependencies.
import js from '@eslint/js'
import globals from 'globals'
import react from 'eslint-plugin-react'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      ...tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    plugins: {
      react,
    },
    languageOptions: {
      globals: globals.browser,
      parserOptions: { ecmaFeatures: { jsx: true } },
    },
    rules: {
      // react-hooks v7 introduced set-state-in-effect which incorrectly flags
      // legitimate async data-fetching patterns documented by the React team.
      'react-hooks/set-state-in-effect': 'off',
      // Prevents regressing #299: an un-typed <button> inside a <form>
      // defaults to type="submit" and triggers unintended form submission.
      'react/button-has-type': 'error',
    },
  },
])
