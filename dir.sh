#!/bin/zsh


set -e

# Main Java Scripts
for file in src/phylodynamics/**/*.java(N); do
  rel=${file#src/}
  dest="src/main/java/$rel"
  mkdir -p "$dest:h"
  mv "$file" "$dest"
done

# Test folder
mkdir -p src/test/java/phylodynamics/epidemiology
if [[ -d src/test/phylodynamics/epidemiology ]]; then
  mv src/test/phylodynamics/epidemiology/*(N) src/test/java/phylodynamics/epidemiology/
fi

# fxtemplates folder to resources
mkdir -p src/main/resources
if [[ -d fxtemplates ]]; then
  mv fxtemplates/*(N) src/main/resources/
fi

# Empty pom.xml file
touch pom.xml

# Remove old copies
rm -rf src/phylodynamics
rm -rf src/test/phylodynamics
rm -rf fxtemplates
rm -rf build
rm -rf release
rm -f build.xml
