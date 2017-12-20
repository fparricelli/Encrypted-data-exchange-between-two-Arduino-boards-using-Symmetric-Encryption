#include <UtilityMessaging.h>

#define LED_GREEN 12
#define LED_RED 11

/* Finale */
static SHA256 sha;
static CBC<AES128> cbc;
static uint8_t my_k[TOKEN_SIZE];
static uint8_t my_f[TOKEN_SIZE];
static uint8_t aes_key[KEY_SIZE];
static uint8_t aes_iv[KEY_SIZE];
static int in_seq_no = 0;
static int out_seq_no = 0;
static int x;

String message;
String decrypted_string;

void setup() {

  RNG.begin("TestCurve25519 1.0", 950);
  
  Serial.begin(9600);
  //pinMode(LED_GREEN, OUTPUT);
 // pinMode(LED_RED, OUTPUT);
  
}

void loop() {

  /*  Getting command */

  String cmd = get_message_from_serial();

  if(cmd.equals("1"))
    send_token_shared();
  else if(cmd.equals("2"))
    received_token_shared();
  else if(cmd.equals("3"))
    encrypt_message();
  else if(cmd.equals("4"))
    decrypt_message();

}

void send_token_shared(){

  message = get_message_from_serial();

  if(message.toInt() == 1)
    x = 0;  //STARTER
  else
    x = 1; //NOT-STARTER

     in_seq_no = 0;
     out_seq_no = 0;
  
  Curve25519::dh1(my_k, my_f);

  send_encoded_message_to_serial(my_k, TOKEN_SIZE);

}

void received_token_shared(){

  uint8_t* other_k;
  other_k = (uint8_t*) malloc(TOKEN_SIZE);

  get_decoded_message_from_serial(other_k, TOKEN_SIZE);
  
  bool exchange = Curve25519::dh2(other_k, my_f);

  memcpy(my_k, other_k, 32);

  free(other_k);

  if(exchange)
    send_encoded_message_to_serial("OK");
  else
    send_encoded_message_to_serial("NACK");

}

void encrypt_message(){

  message = get_message_from_serial();

  out_seq_no++;
  out_seq_no = 2*out_seq_no+x;

  message = String(out_seq_no) + "#" + message;

  uint8_t* encrypted;
  encrypted = (uint8_t*) malloc(MAX_SIZE);

  uint8_t* msg_key;
  msg_key = (uint8_t*) malloc(HASH_SIZE);

  compute_msg_key(message, message.length(), 0, &sha, msg_key);

  send_encoded_message_to_serial(msg_key, HASH_SIZE);

  derive_key(msg_key, my_k, aes_key, aes_iv);

  free(msg_key);
  
  cbc_aes128_encrypt(&message, encrypted, aes_key, aes_iv, &cbc);

  send_encoded_message_to_serial(encrypted, MAX_SIZE);

  free(encrypted);

}

void decrypt_message(){

  int* expected_seq_no = malloc(sizeof(int));
  in_seq_no++;
  if(x==0)
    *expected_seq_no = 2*in_seq_no +1;
  else
    *expected_seq_no = 2*in_seq_no;

  uint8_t* decrypted;
  decrypted = (uint8_t*) malloc(MAX_SIZE);

  uint8_t* encrypted;
  encrypted = (uint8_t*) malloc(MAX_SIZE);

  uint8_t* msg_key;
  msg_key = (uint8_t*) malloc(HASH_SIZE);

    uint8_t* msg_key2;
  msg_key2 = (uint8_t*) malloc(HASH_SIZE);

  get_decoded_message_from_serial(msg_key, HASH_SIZE);

  get_decoded_message_from_serial(encrypted, MAX_SIZE);

  /* Deriva Chiave */

  derive_key(msg_key, my_k, aes_key, aes_iv);

  cbc_aes128_decrypt(encrypted, decrypted, aes_key, aes_iv, &cbc);

  decrypted_string = (char *) decrypted;
  
  compute_msg_key(decrypted_string, decrypted_string.length(), 0, &sha, msg_key2);

  if(!memcmp(msg_key,msg_key2, HASH_SIZE))
    send_encoded_message_to_serial("HASHING NOT MATCHING");
  else {
      extract_seq_no(&decrypted_string, decrypted_string.length(), &in_seq_no);
      if(in_seq_no == *expected_seq_no){
        decrypted_string.toCharArray(decrypted, MAX_SIZE);
        send_encoded_message_to_serial(decrypted);
      }
      else
        send_encoded_message_to_serial("wrong");
  }

  free(decrypted);

  free(msg_key);

  free(msg_key2);

  free(expected_seq_no);
  
}

