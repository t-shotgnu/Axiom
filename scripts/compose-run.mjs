#!/usr/bin/env node
/**
 * Reliable Docker Compose entrypoint across environments:
 * - Prefer `docker-compose` when present, then fall back to `docker compose`.
 * - No shell interpolation so flags like `-d` are never misparsed.
 */
import { spawnSync } from 'node:child_process';
import { setTimeout as delay } from 'node:timers/promises';

const composeArgs = process.argv.slice(2);

function tryRun(binary, argv) {
  const result = spawnSync(binary, argv, { stdio: 'inherit', shell: false });

  if (result.error?.code === 'ENOENT') {
    return 'missing';
  }
  if (result.signal) {
    return 'fail';
  }
  return result.status === 0 ? 'ok' : 'fail';
}

function runCompose(cmdArgs) {
  let outcome = tryRun('docker-compose', cmdArgs);
  if (outcome === 'ok') {
    return 0;
  }

  outcome = tryRun('docker', ['compose', ...cmdArgs]);
  if (outcome === 'ok') {
    return 0;
  }

  return 1;
}

async function main() {
  let exitCode = runCompose(composeArgs);

  if (
    exitCode !== 0 &&
    composeArgs[0] === 'up' &&
    composeArgs.includes('--wait')
  ) {
    const withoutWait = composeArgs.filter((a) => a !== '--wait');
    exitCode = runCompose(withoutWait);
    if (exitCode === 0 && process.platform !== 'win32') {
      await delay(3000);
    }
  }

  if (exitCode !== 0) {
    console.error(
      '\nCould not run Docker Compose. Ensure either `docker-compose` or `docker compose` works ',
      '(Docker Desktop installs both on macOS).\n',
    );
  }

  process.exit(exitCode);
}

main();
