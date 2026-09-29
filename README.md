# gitlet

A version control system, written in Java, that mimics some of the basic features of Git.

It's my implementation of CS 61B's [Gitlet project](https://sp21.datastructur.es/materials/proj/proj2/proj2).

## Build

```bash
make build # packages into target/gitlet.jar
```

## Usage

```bash
# Create a new gitlet repository in the current directory
java -jar target/gitlet.jar init

# Add a new or changed file to the staging area
java -jar target/gitlet.jar add "filename"

# Create a new commit with a message
java -jar target/gitlet.jar commit "message"

# Show commit logs
java -jar target/gitlet.jar log
```

## TODO

- [x] init
- [x] add
- [x] commit
- [x] checkout
- [x] log
- [ ] rm
- [ ] status
- [ ] branch
- [ ] reset
- [ ] merge
