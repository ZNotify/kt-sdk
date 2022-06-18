const fs = require('fs');
const contents = fs.readFileSync("gradle.properties").toString();
const lines = contents.split("\n")
for (const line of lines) {
    const [key, value] = line.trim().split("=");
    if (key === "kotlin.version"){
        console.log(value);
        return value;
    }
}