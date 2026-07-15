'use strict';

const fs = require('node:fs');
const path = require('node:path');
const express = require('express');
const questpdf = require('questpdf');
const shared = require('../shared/test-document.cjs');

const app = express();

app.get('/health', (request, response) => {
    response.send('OK');
});

// Generates all test documents into TestOutput and returns the qpdf-merged PDF.
app.get('/generate', (request, response) => {
    const outputFolder = shared.runTests(questpdf);
    response.type('application/pdf').send(fs.readFileSync(path.join(outputFolder, 'qpdf.pdf')));
});

const port = Number(process.env.PORT ?? 5087);

app.listen(port, '127.0.0.1', () => {
    console.log(`Listening on http://127.0.0.1:${port}`);
});
