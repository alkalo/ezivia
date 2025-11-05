#!/usr/bin/env node

const { spawn } = require('child_process');
const net = require('net');

function parsePort(value) {
  const portNumber = Number(value);
  if (!Number.isInteger(portNumber) || portNumber <= 0 || portNumber > 65535) {
    throw new Error(`Invalid port value: ${value}`);
  }
  return portNumber;
}

function detectExplicitPortFromArgs(args) {
  const passthroughArgs = [];
  let explicitPort = null;

  for (let i = 0; i < args.length; i += 1) {
    const arg = args[i];

    if (arg === '-p' || arg === '--port') {
      const nextValue = args[i + 1];
      if (!nextValue) {
        throw new Error('Missing value for port argument.');
      }
      explicitPort = parsePort(nextValue);
      i += 1;
      continue;
    }

    if (arg.startsWith('--port=')) {
      explicitPort = parsePort(arg.split('=')[1]);
      continue;
    }

    passthroughArgs.push(arg);
  }

  return { explicitPort, passthroughArgs };
}

function isPortAvailable(port, host = '0.0.0.0') {
  return new Promise((resolve) => {
    const server = net.createServer();

    server.once('error', (error) => {
      if (error.code === 'EADDRINUSE' || error.code === 'EACCES') {
        resolve(false);
      } else {
        resolve(false);
      }
    });

    server.once('listening', () => {
      server.close(() => resolve(true));
    });

    server.listen(port, host);
  });
}

async function findAvailablePort(startPort, attempts = 20) {
  let port = startPort;
  for (let attempt = 0; attempt < attempts; attempt += 1) {
    // eslint-disable-next-line no-await-in-loop
    const available = await isPortAvailable(port);
    if (available) {
      return port;
    }
    port += 1;
  }
  throw new Error(`Could not find an available port starting from ${startPort}.`);
}

async function main() {
  const rawArgs = process.argv.slice(2);
  let explicitPort = null;
  let passthroughArgs = rawArgs;

  try {
    const result = detectExplicitPortFromArgs(rawArgs);
    explicitPort = result.explicitPort;
    passthroughArgs = result.passthroughArgs;
  } catch (error) {
    console.error(error.message);
    process.exit(1);
  }

  let envPort = null;
  if (process.env.PORT) {
    try {
      envPort = parsePort(process.env.PORT);
    } catch (error) {
      console.error(`Invalid PORT environment variable: ${error.message}`);
      process.exit(1);
    }
  }

  const defaultPort = 3000;
  const requestedPort = explicitPort ?? envPort ?? defaultPort;
  let selectedPort;

  try {
    selectedPort = await findAvailablePort(requestedPort);
  } catch (error) {
    console.error(error.message);
    process.exit(1);
  }

  if (selectedPort !== requestedPort) {
    if (explicitPort !== null || envPort !== null) {
      console.error(`Requested port ${requestedPort} is not available. Please choose a different port.`);
      process.exit(1);
    }

    console.warn(`Port ${requestedPort} is in use. Starting Next.js on available port ${selectedPort} instead.`);
  }

  const nextBin = require.resolve('next/dist/bin/next');
  const finalArgs = ['start', ...passthroughArgs, '-p', String(selectedPort)];
  const child = spawn(process.execPath, [nextBin, ...finalArgs], {
    stdio: 'inherit',
    env: { ...process.env, PORT: String(selectedPort) },
  });

  child.on('exit', (code, signal) => {
    if (signal) {
      process.kill(process.pid, signal);
      return;
    }
    process.exit(code ?? 0);
  });

  child.on('error', (error) => {
    console.error('Failed to start Next.js:', error);
    process.exit(1);
  });
}

main();
