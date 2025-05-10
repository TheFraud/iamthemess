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

our @PREFIX                     = ( '/home/safevision/Desktop/safevision.tech/openssl-3.3.2' );
our @libdir                     = ( '/home/safevision/Desktop/safevision.tech/openssl-3.3.2' );
our @BINDIR                     = ( '/home/safevision/Desktop/safevision.tech/openssl-3.3.2/apps' );
our @BINDIR_REL_PREFIX          = ( 'apps' );
our @LIBDIR                     = ( '/home/safevision/Desktop/safevision.tech/openssl-3.3.2' );
our @LIBDIR_REL_PREFIX          = ( '' );
our @INCLUDEDIR                 = ( '/home/safevision/Desktop/safevision.tech/openssl-3.3.2/include', '/home/safevision/Desktop/safevision.tech/openssl-3.3.2/include' );
our @INCLUDEDIR_REL_PREFIX      = ( 'include', './include' );
our @APPLINKDIR                 = ( '/home/safevision/Desktop/safevision.tech/openssl-3.3.2/ms' );
our @APPLINKDIR_REL_PREFIX      = ( 'ms' );
our @ENGINESDIR                 = ( '/home/safevision/Desktop/safevision.tech/openssl-3.3.2/engines' );
our @ENGINESDIR_REL_LIBDIR      = ( 'engines' );
our @MODULESDIR                 = ( '/home/safevision/Desktop/safevision.tech/openssl-3.3.2/providers' );
our @MODULESDIR_REL_LIBDIR      = ( 'providers' );
our @PKGCONFIGDIR               = ( '/home/safevision/Desktop/safevision.tech/openssl-3.3.2' );
our @PKGCONFIGDIR_REL_LIBDIR    = ( '.' );
our @CMAKECONFIGDIR             = ( '/home/safevision/Desktop/safevision.tech/openssl-3.3.2' );
our @CMAKECONFIGDIR_REL_LIBDIR  = ( '.' );
our $VERSION                    = '3.3.2';
our @LDLIBS                     =
    # Unix and Windows use space separation, VMS uses comma separation
    $^O eq 'VMS'
    ? split(/ *, */, '-ldl -pthread ')
    : split(/ +/, '-ldl -pthread ');

1;
