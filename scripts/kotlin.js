// Path: scripts\kotlin.js

const fs = require('fs');
const path = require('path');

const file = fs.readFileSync(path.join(__dirname, '../build.gradle.kts'), 'utf8');
const match = file.match(/val ktVersion = "(.*)"/);
const version = match[1];

console.log(version);

fs.mkdirSync(path.join(__dirname, '../build'), { recursive: true });
fs.writeFileSync(path.join(__dirname, '../build/kotlinVersion.txt'), version, 'utf8');