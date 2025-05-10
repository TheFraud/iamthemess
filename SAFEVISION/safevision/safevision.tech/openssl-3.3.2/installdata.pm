package OpenSSL::safe::installdata;

use strict;
use warnings;
use Exporter;
our @ISA = qw(Exporter);
our @EXPORT = qw(
    @PREFIX
    @libdir
    @BINDIR @BINDIR_REL_PREFIX
    @LIBDIR @LIBDIR_REL_PREFIX
    @INCLUDEDIR @INCLUDEDIR_REL_PREFIX
    @APPLINKDIR @APPLINKDIR_REL_PREFIX
    @ENGINESDIR @ENGINESDIR_REL_LIBDIR
    @MODULESDIR @MODULESDIR_REL_LIBDIR
    @PKGCONFIGDIR @PKGCONFIGDIR_REL_LIBDIR
    @CMAKECONFIGDIR @CMAKECONFIGDIR_REL_LIBDIR
    $VERSION @LDLIBS
);

our @PREFIX                     = ( '/home/safevision/Desktop/safevision.tech/app/src/main/cpp/openssl3_include' );
our @libdir                     = ( '/home/safevision/Desktop/safevision.tech/app/src/main/cpp/openssl3_include/lib' );
our @BINDIR                     = ( '/home/safevision/Desktop/safevision.tech/app/src/main/cpp/openssl3_include/bin' );
our @BINDIR_REL_PREFIX          = ( 'bin' );
our @LIBDIR                     = ( '/home/safevision/Desktop/safevision.tech/app/src/main/cpp/openssl3_include/lib' );
our @LIBDIR_REL_PREFIX          = ( 'lib' );
our @INCLUDEDIR                 = ( '/home/safevision/Desktop/safevision.tech/app/src/main/cpp/openssl3_include/include' );
our @INCLUDEDIR_REL_PREFIX      = ( 'include' );
our @APPLINKDIR                 = ( '/home/safevision/Desktop/safevision.tech/app/src/main/cpp/openssl3_include/include/openssl' );
our @APPLINKDIR_REL_PREFIX      = ( 'include/openssl' );
our @ENGINESDIR                 = ( '/home/safevision/Desktop/safevision.tech/app/src/main/cpp/openssl3_include/lib/engines-3' );
our @ENGINESDIR_REL_LIBDIR      = ( 'engines-3' );
our @MODULESDIR                 = ( '/home/safevision/Desktop/safevision.tech/app/src/main/cpp/openssl3_include/lib/ossl-modules' );
our @MODULESDIR_REL_LIBDIR      = ( 'ossl-modules' );
our @PKGCONFIGDIR               = ( '/home/safevision/Desktop/safevision.tech/app/src/main/cpp/openssl3_include/lib/pkgconfig' );
our @PKGCONFIGDIR_REL_LIBDIR    = ( 'pkgconfig' );
our @CMAKECONFIGDIR             = ( '/home/safevision/Desktop/safevision.tech/app/src/main/cpp/openssl3_include/lib/cmake/OpenSSL' );
our @CMAKECONFIGDIR_REL_LIBDIR  = ( 'cmake/OpenSSL' );
our $VERSION                    = '3.3.2';
our @LDLIBS                     =
    # Unix and Windows use space separation, VMS uses comma separation
    $^O eq 'VMS'
    ? split(/ *, */, '-ldl -pthread ')
    : split(/ +/, '-ldl -pthread ');

1;
