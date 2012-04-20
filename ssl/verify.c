/*  Digital signature verification for Gaggle tracks
 *  Copyright (C) 2012 Marc Poulhiès
 *
 *  This program is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU General Public License for more details. 
 *  You should have received a copy of the GNU General Public License 
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

#include <stdio.h>
#include <stdlib.h>

#include <openssl/sha.h>
#include <openssl/bio.h>
#include <openssl/rsa.h>
#include <openssl/pem.h>
#include <openssl/err.h>

#include <string.h>
#include <openssl/hmac.h>
#include <openssl/evp.h>
#include <openssl/buffer.h>

static const char VERIF_OK[] = "Signature verification OK\n";
static const char VERIF_FAIL[] = "Signature verification FAILED\n";

/*
 * The **PUBLIC** key in PEM format
 */
unsigned char pkey_b64[] = "-----BEGIN PUBLIC KEY-----\n"
  ".....\n"
  ".....\n"
  "-----END PUBLIC KEY-----\n";


unsigned char sig_b64[1024];
int sig_b64_size;

unsigned char sig[1024];
int sig_len;

void bailOut(const char* err, int ssl){
  if (err){
    fprintf(stderr, "ERROR: %s\n", err);
  }

  if (ssl)
    fprintf(stderr, "OpenSSL error: %s\n", ERR_error_string(ERR_get_error(), NULL));

  exit (-1);
}

int unbase64(unsigned char *input, int input_len, unsigned char*output, int output_len) {
  BIO *b64, *bmem;
  int size;

  /* char *buffer = malloc(length); */
  memset(output, 0, output_len);

  b64 = BIO_new(BIO_f_base64());
  bmem = BIO_new_mem_buf(input, input_len);
  BIO_push(b64, bmem);

  size = BIO_read(b64, output, output_len);
  BIO_free_all(b64);
  return size;
}

int verify_data(const char* igcfile) {
  BIO *b = NULL;
  RSA *r = NULL;

  int rc = 1; /* OpenSSL return code */ 

  char buf[256];
  int buf_len = 0;

  SHA_CTX sha_ctx = { 0 };
  unsigned char digest[SHA_DIGEST_LENGTH];

  rc = SHA1_Init(&sha_ctx);
  if (1 != rc) { bailOut(NULL, 1); }

  FILE *f = fopen(igcfile, "r");

  if (!f) bailOut("Can't open igc file", 0);

  int c;
  int new_line = 1;
  int fill_sig = 0;
  int skip_line = 0;

  while ( (c = fgetc(f)) != EOF){
    if (new_line) {
      if (c == 'G') {
        fill_sig = 1;
        // fill b64 sig
      } else if (c == 'L'){
	// comment, skip line, do not fill sig buffer
	skip_line = 1;
	fill_sig = 0;
      } else {
        // fill buffer
        buf[buf_len] = c;
        buf_len++;
      }
      new_line = 0;
    } else {
      if (c == '\n') {
        new_line = 1;
      }
      if (fill_sig){
        sig_b64[sig_b64_size] = c;
        sig_b64_size++;
      } else if (skip_line) {
	// do nothing
      } else {
        // fill buffer
        buf[buf_len] = c;
        buf_len++;
      }
    }

    if (buf_len == sizeof(buf) || (buf_len && fill_sig)){
      rc = SHA1_Update(&sha_ctx, buf, buf_len);
      if (1 != rc) { bailOut(NULL, 1); }
      buf_len = 0;
    }
  }

  sig_b64[sig_b64_size] = 0;

  // decode b64 signature
  sig_len = unbase64(sig_b64, sig_b64_size, sig, sizeof(sig));

  rc = SHA1_Final(digest, &sha_ctx);
  if (1 != rc) {  bailOut(NULL, 1); }

  b = BIO_new_mem_buf(pkey_b64, sizeof(pkey_b64));
  PEM_read_bio_RSA_PUBKEY(b, &r, NULL, NULL);

  rc = RSA_verify(NID_sha1, digest, sizeof (digest), sig, sig_len, r);

  // verification went well.
  if (NULL != r) RSA_free(r);
  if (NULL != b) BIO_free(b);

  return (rc == 1);
}

int main(int argc, char **argv){
  int r;
  if (argc != 2){
    bailOut("Must have exactly one argument ( file name for IGC track to verify)",0);
  }

  r =  verify_data(argv[1]);
  if (r){
    printf(VERIF_OK);
  } else {
    printf(VERIF_FAIL);
  }
  return r;
}
