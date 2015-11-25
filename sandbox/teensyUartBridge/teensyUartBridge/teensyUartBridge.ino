#include "SimpleTimer.h"

#define HWSERIAL Serial2

#define BL600_BAUD 9600
#define PC_BAUD 9600

#define NUM_LED_VALUES 8

int ledPin = 13;

SimpleTimer timer;
bool ledValues[NUM_LED_VALUES] = { 1, 0, 1, 0, 0, 0, 0, 0 };
int ledValueIdx = 0;

void blinkLED()
{
  // write current led value
  digitalWrite(ledPin, ledValues[ledValueIdx]);
  ledValueIdx = (ledValueIdx + 1) % NUM_LED_VALUES;  
}

void setup() 
{
  pinMode(ledPin, OUTPUT);

  // configure serial ports @ 9600
  Serial.begin(PC_BAUD);
  HWSERIAL.begin(BL600_BAUD);  

  delay(200);
  Serial.println("Listening...");

  // configure timer
  timer.setInterval(150, blinkLED);
}

void loop() {

  timer.run();

  // pass data from BL600 to PC
  if (HWSERIAL.available())
  {
    int inByte = HWSERIAL.read();
    Serial.write(inByte);
  }

  // pass data from PC to BL600
  // ---------------------------------------
  // cls: data must be followed by carriage return in order for
  // the BL600 to interpret as a command (i.e. this code does
  // NOT provide the carriage return - the serial terminal should
  // be configured to append a carriage return to each line).
  if (Serial.available())
  {
    int serialByte = Serial.read();
    HWSERIAL.write(serialByte);    
  }
}
