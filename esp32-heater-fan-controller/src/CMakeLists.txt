# This file was automatically generated for projects
# without default 'CMakeLists.txt' file.
# FILE(GLOB_RECURSE app_sources ${CMAKE_SOURCE_DIR}/src/*.*)
# idf_component_register(SRCS ${app_sources})

# Uncomment the one you want to build/flash
idf_component_register(
    SRCS
        main.c
        # tests/relay_test.c
        # tests/hivemq_test.c
)
