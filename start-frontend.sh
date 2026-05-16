#!/bin/bash

cd "$(dirname "$0")"

echo "Starting Frontend..."
cd frontend

if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

npm run dev
