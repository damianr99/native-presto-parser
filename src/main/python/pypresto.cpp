#include <pybind11/pybind11.h>

#include "NativePrestoParser.h"

std::string simpleParse(const std::string& sql) {
  graal_isolate_t *isolate = NULL;
  graal_isolatethread_t *thread = NULL;

  if (graal_create_isolate(NULL, &isolate, &thread) != 0)
    return "graal_create_isolate";

  std::string ret = parse(thread, (char*)sql.c_str());

  if (graal_detach_thread(thread) != 0)
    return "graal_detach_thread";

  return ret;
}

namespace py = pybind11;

PYBIND11_MODULE(pypresto, m) {
  m.doc() = "presto native parser";
  m.def("parse", &simpleParse, "Parse SQL string");
}
