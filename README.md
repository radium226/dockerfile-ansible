# Dockerfile.yml

## Goal
I wanted this kind of tool after discovering that [Ansible Container](https://www.ansible.com/integrations/containers/ansible-container) is a great way to use Ansible roles to create Docker images but it fails with a single image because it's heavy rely on Jinja templates in order to configure components... And it's better to use `ENV_VAR` when using Docker!  

I wanted a KISS tool which only provide an abstraction over standard Dockerfile, and that's all! 

## Usage
`dockerfile-yaml [ --var key1=value1 [ --var key2=value2 ] ] [ --config=/path/to/config.conf ] /path/to/Dockerfile.yml [ /path/to/output/folder ]`

## Syntax
```
from: "ubuntu"
tasks:
 - echo: "coucou"
 - include: "java.yml"
 - copy:
    content: |
      I have no idea what
      I'm doing
    dest: "/tmp/w00t.txt"
 - get_url:
    url: "http://example.com/archive.zip"
    dest: "/tmp/archive.zip"
 - file:
    path: "{{ item }}"
    state: directory
   with_items:
    - "/tmp/first-directory"
    - "/tmp/second-directory"
 - package:
    name: "{{ item }}"
   with_items:
    - "vim"
    - "emacs"
 - shell: |
    #!/bin/bash
    echo "I'm a standalone script"
execute:
  java:
    jar: "app.jar"
```

## To Do
- [ ] Replace `Distro => Seq[Statement]` by `Distro => Validated[Seq[Statement]]`
- [ ] Allow a way to add more tasks in a plugin way
- [ ] Handle the quote better
