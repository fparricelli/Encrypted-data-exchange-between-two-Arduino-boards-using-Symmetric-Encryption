#include <Arduino.h>
#include <Crypto.h>
#include <AES.h>
#include <Curve25519.h>
#include <CBC.h>
#include <SHA256.h>
#include <string.h>
#include "rBase64.h"
#include <RNG.h>

#define HASH_SIZE 16//256-bit

#define KEY_SIZE 16 //128-bit

#define MAX_SIZE 64

#define TOKEN_SIZE 32


/*
	Utility Function for interaction with Serial RXTX
*/

String get_message_from_serial();

void convert_string_to_byte_array(String , byte* );

void AES128_set_key(byte* , AES128* );

void get_decoded_message_from_serial(byte* , unsigned int );

void AES128_encrypt(byte* , byte*, AES128*);
void AES128_decrypt(byte* , byte* , AES128*);

void send_encoded_message_to_serial(byte*);

void send_encoded_message_to_serial(byte* , unsigned int);

/*
 * Library for generate the secret chat key
 */

void get_SHA256(char* , size_t , SHA256* , uint8_t* );
void get_SHA256(String , size_t , SHA256* , uint8_t* );
void compute_msg_key(String, uint8_t* , int , SHA256* , uint8_t* );
void derive_key(uint8_t* , uint8_t* ,  uint8_t* , uint8_t* );
void first_phase_share_token(uint8_t* , uint8_t* );
bool second_phase_share_token(uint8_t* , uint8_t* );
void cbc_aes128_encrypt(String* , uint8_t* , uint8_t* , uint8_t* , CBC<AES128>* );
void cbc_aes128_decrypt(uint8_t* , String* , uint8_t* , uint8_t* , CBC<AES128>* );
void cbc_aes128_decrypt(uint8_t* , uint8_t* , uint8_t* , uint8_t* , CBC<AES128>* );
void extract_seq_no(String* , size_t , int* );


