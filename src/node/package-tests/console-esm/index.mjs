// Default imports keep the interop with the CommonJS build of the package (and
// the shared CJS test module) independent of Node's named-export detection.
import questpdf from 'questpdf';
import shared from '../shared/test-document.cjs';

const outputFolder = shared.runTests(questpdf);
console.log(`Documents generated into ${outputFolder}`);
