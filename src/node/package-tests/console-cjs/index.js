'use strict';

const questpdf = require('questpdf');
const shared = require('../shared/test-document.cjs');

const outputFolder = shared.runTests(questpdf);
console.log(`Documents generated into ${outputFolder}`);
