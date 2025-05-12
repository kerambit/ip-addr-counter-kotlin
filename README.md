### IpV4 Address Counter

App calculates the number of unique IPv4 addresses in a given file and print the result.

### Features:

- Processing large files
- Supports coroutines for faster processing

### Usage

Supports two modes of operation:

1. Without coroutines. Requires less memory, but slower

Use file pathname parameter to specify working file:

```bash
-Dfile=/Path/to/file/ips_txt
```

2. With coroutines. Requires more memory, but faster

Use file pathname parameter to specify working file and add parallel working mode flag

```bash
-Dfile=/Path/to/file/ips_txt -DisParallel=true
```

### Benchmarks

Tested on a large [file](https://ecwid-vgv-storage.s3.eu-central-1.amazonaws.com/ip_addresses.zip).

Without coroutines:
```bash
Unique ips:  1000000000
Total time: 8m01s140ms
```

With coroutines (5 workers):
```bash
Unique ips:  1000000000
Total time: 3m40s599ms
```