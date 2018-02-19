# Dockerfile.yml

## Goal
I wanted this kind of tool after discovering that [Ansible Container](https://www.ansible.com/integrations/containers/ansible-container) is a great way to use Ansible roles to create Docker images but it fails with a single image because it's heavy leverage of Jinja template in order to configure connection between component. 

I wanted a KISS tool which only provide an abstraction over standard Dockerfile, and that's all! 

## Usage
`dockerfile-yaml [ --config=/path/to/config.conf ] /path/to/Dockerfile.yml [ /path/to/output/folder ]`

## To Do
- [ ] Replace `Distro => Seq[Statement]` by `Distro => Validated[Seq[Statement]]`
- [ ] Allow a way to add more tasks in a plugin way
