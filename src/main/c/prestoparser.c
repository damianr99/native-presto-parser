#include <stdio.h>
#include <stdlib.h>

#include "NativePrestoParser.h"

static void die(const char *msg) {
  fprintf(stderr, "%s\n", msg);
  exit(1);
}

static char* read_stdin() {
  char *buffer = NULL;
  size_t len;
  ssize_t bytes_read = getdelim(&buffer, &len, '\0', stdin);
  if (bytes_read == -1)
    die("getdelim");
  return buffer;
}

int main() {
  graal_isolate_t *isolate = NULL;
  graal_isolatethread_t *thread = NULL;

  if (graal_create_isolate(NULL, &isolate, &thread) != 0)
    die("graal_create_isolate");

  puts(parse(thread, read_stdin()));

  if (graal_detach_thread(thread) != 0)
    die("graal_detach_thread");
}
