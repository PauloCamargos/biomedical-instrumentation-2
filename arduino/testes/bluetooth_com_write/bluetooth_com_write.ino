#include "SoftwareSerial.h"
#include "Statistics.h" 
//https://github.com/provideyourown/statistics
#include "pitches.h"

#define TX 11 // pino tx
#define RX 12 // pino rx
#define ANALOG_PIN A7 // leitua do tecido
#define SOUND_PIN 13 // ouput buzzer
#define NOTE_FRACTION 20 // fracao de segundo do som
#define NOTE NOTE_A7 // nota musical do buzzer

// instanciando pino tx e rx do bluetooth
SoftwareSerial blth(RX,TX);

// instanciando Statistics com 10 valores
Statistics stat(50);
int count = 0;
void setup() {
  //inciando comunicacao no serial monitor e bluetooth
  //Serial.begin(115200);
  blth.begin(9600);

  // iniciando estatistica (stat)
  stat.reset();
}

void loop() {
  // valor lido do tecido, entre 0-255
  byte val = map(analogRead(ANALOG_PIN), 0,1023,0,255);
  byte mappedVal = map(val,40,70,0,255);
  
  // incluindo o valor no array de stat
  stat.addData(mappedVal);

  // media e desvio padrao
  float meanVal = stat.mean();
  float stdDev = stat.stdDeviation();

  int mappedMean = map(meanVal,110,190, 0,255);

  // soa alarme de desvio padrao < 40
  if(stdDev <= 40){
      triggerAlarm();
  }

  //int mapped_value = map(mean_val, 40,80,0,255); 

  // exibindo no serial monitor
//  Serial.println("A " + String (mappedVal));
//  Serial.println("L " + String (limite));

//  Serial.println("S " + String (stdDev));
//  Serial.println("M " + String (mean_val) + "\n----");

//  Serial.println(val);
  // enviando para smartphone
  
  blth.print(mappedVal);
  blth.print("\n");
  delay(80);
}

void triggerAlarm(){
  int noteDuration = 1000 / NOTE_FRACTION;
  tone(13, NOTE ,noteDuration);
}
