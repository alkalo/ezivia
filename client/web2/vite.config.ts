import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  base: '/WEB2/',
  build: {
    outDir: 'dist',
    emptyOutDir: true
  },
  preview: {
    port: 3002,
    host: '0.0.0.0'
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './vitest.setup.ts'
  }
});
