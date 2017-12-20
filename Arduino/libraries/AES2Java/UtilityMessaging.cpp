#include "UtilityMessaging.h"

String get_message_from_serial(){
	while (!Serial.available());
	  return Serial.readString();
}

void convert_string_to_byte_array(String input, byte* output){
  char* input_char = (char*) malloc(input.length()+1);
  input.toCharArray(input_char, input.length()+1);
  for(int j = 0; j<input.length()+1; j++)
    output[j] = byte(input_char[j]);
    
    free(input_char);
}

void AES128_set_key(byte* key, AES128* aes128){
	 aes128->setKey(key, aes128->keySize());
}

void get_decoded_message_from_serial(byte* output, unsigned int size){
	  String c_message = get_message_from_serial();
	  char c_message_char[c_message.length()];
	  c_message.toCharArray(c_message_char, c_message.length());
	  char c_message_dec[size];
	  rbase64_decode(c_message_dec,c_message_char, c_message.length());
	    for(int j = 0; j<size; j++)
	      output[j] = byte(c_message_dec[j]);
}


void AES128_encrypt(byte* input, byte* output, AES128* aes128){
	 aes128->encryptBlock(output, input);
}
void AES128_decrypt(byte* input, byte* output, AES128* aes128){
	 aes128->decryptBlock(output, input);
}


void send_encoded_message_to_serial(byte* message){
	String dec = rbase64.encode(message);
	Serial.println(dec);
}

void send_encoded_message_to_serial(byte* message, unsigned int size){
	String dec = rbase64.encode(message,size);
	Serial.println(dec);
}


/* Input Array Char*/

void get_SHA256(char* payload, size_t payload_size, SHA256* sha, uint8_t* output){
    sha->reset();
    sha->update(payload, payload_size);
    sha->finalize(output, HASH_SIZE);
}
/* Input String*/

void get_SHA256(String str_msg, size_t msg_size, SHA256* sha, uint8_t* hash){
    char* msg;
    str_msg.toCharArray(msg, msg_size);
    sha->reset();
    sha->update(msg, msg_size);
    sha->finalize(hash, HASH_SIZE);
    free(msg);
}

void compute_msg_key(String str_msg, uint8_t* key, int x, SHA256* sha, uint8_t* msg_key){
    
    uint8_t* hash = malloc(HASH_SIZE);
    
    String* key_string = new String();
    (*key_string) = (char*) key;
    
    String* msg_key_long = new String();
    (*msg_key_long) = key_string->substring(x,24) + str_msg;
    
    char* msg;
    msg_key_long->toCharArray(msg, msg_key_long->length());
    sha->reset();
    sha->update(msg, msg_key_long->length());
    sha->finalize(hash, HASH_SIZE);
    free(msg);
    
    String* msg_key_long_string = new String();
    (*msg_key_long_string)= (char*) hash;
    free(hash);
    
    String* msg_key_string = new String();
    (*msg_key_long_string) = msg_key_long_string->substring(8,16);
    
    msg_key_string->toCharArray(msg_key, KEY_SIZE);
    
    delete(key_string);
    delete(msg_key_string);
    delete(msg_key_long_string);
}


void derive_key(uint8_t* msg_key, uint8_t* dh2_k, uint8_t* aesKey, uint8_t* aesIV){
    
    uint8_t* sha_a;
    sha_a = malloc(HASH_SIZE);
    
    uint8_t* temp;
    temp = malloc(HASH_SIZE);
    
    SHA256* sha;
    sha = new SHA256();
   
    get_SHA256((char*) dh2_k, HASH_SIZE, sha, temp);

    
    /* Combinazione */
    
    for (int i = 0; i<KEY_SIZE; i++) {
        if(i+msg_key[0]%2 == 0)
            aesKey[i]=msg_key[i];
        else
            aesKey[i] = temp[i];
    }
    
        for (int i = 0; i<KEY_SIZE; i++) {
            if(i+temp[0]%2 == 0)
                aesIV[i]=msg_key[i];
            else
                aesIV[i] = temp[i];
        }
    
    delete(sha);
    free(sha_a);
    free(temp);
 
}

void cbc_aes128_encrypt(String* plaintext, uint8_t* encrypted, uint8_t* aes_key, uint8_t* aes_iv, CBC<AES128>* cbc){
    
    char* plaintext_byte;
    plaintext_byte = malloc(sizeof(char)*MAX_SIZE);
    plaintext->toCharArray(plaintext_byte, MAX_SIZE);
    
    cbc->clear();
    cbc->setKey(aes_key, KEY_SIZE);
    cbc->setIV(aes_iv,KEY_SIZE);
    
    cbc->encrypt(encrypted, plaintext_byte, MAX_SIZE);
    
    free(plaintext_byte);
    
}

void cbc_aes128_decrypt(uint8_t* encrypted, String* decrypted, uint8_t* aes_key, uint8_t* aes_iv, CBC<AES128>* cbc){
    
    char* plaintext_byte;
    plaintext_byte = malloc(sizeof(char)*MAX_SIZE);
    
    cbc->clear();
    cbc->setKey(aes_key, KEY_SIZE);
    cbc->setIV(aes_iv,KEY_SIZE);
    
    cbc->decrypt(plaintext_byte, encrypted, MAX_SIZE);
    
    (*decrypted) = String(plaintext_byte);

    free(plaintext_byte);
}

void cbc_aes128_decrypt(uint8_t* encrypted, uint8_t* decrypted, uint8_t* aes_key, uint8_t* aes_iv, CBC<AES128>* cbc){
    
    cbc->clear();
    cbc->setKey(aes_key, KEY_SIZE);
    cbc->setIV(aes_iv,KEY_SIZE);
    
    cbc->decrypt(decrypted, encrypted, MAX_SIZE);
    
}

void extract_seq_no(String* decrypted_string, size_t dim, int* seq_no){
    
    int* position = malloc(sizeof(int));
    
    *position = decrypted_string->indexOf('#');
    
    *seq_no = (decrypted_string->substring(0, *position)).toInt();
    
    *decrypted_string = decrypted_string->substring(*position +1);
    
    free(position);
    
    
}



