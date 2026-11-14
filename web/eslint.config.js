import js from "@eslint/js";
import tseslintPlugin from "@typescript-eslint/eslint-plugin";
import tsParser from "@typescript-eslint/parser";
import react from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";

export default [
  {
    ignores: ["dist/**", "node_modules/**", "playwright-report/**", "test-results/**", "coverage/**"],
  },
  js.configs.recommended,
  react.configs.flat.recommended,
  reactHooks.configs["recommended-latest"],
  {
    files: ["**/*.{ts,tsx}"],
    languageOptions: {
      parser: tsParser,
    },
    plugins: {
      "@typescript-eslint": tseslintPlugin,
    },
    rules: {
      ...tseslintPlugin.configs.recommended.rules,
      // TypeScript's own compiler already catches undefined-reference errors more
      // accurately than eslint's no-undef, which doesn't understand TS types/ambient
      // globals and produces false positives on this codebase's DOM/vitest globals.
      "no-undef": "off",
      "react/react-in-jsx-scope": "off",
    },
    settings: {
      react: { version: "detect" },
    },
  },
];
