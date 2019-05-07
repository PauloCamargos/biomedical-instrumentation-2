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
Statistics stat(10);

void setup() {
  //inciando comunicacao no serial monitor e bluetooth
  Serial.begin(9600);
  blth.begin(9600);

  // iniciando estatistica (stat)
  stat.reset();
}

void loop() {
  // valor lido do tecido, entre 0-255
  //byte val = map(analogRead(ANALOG_PIN), 0,1023,0,255);
  float val = (analogRead(ANALOG_PIN) * 5.0 )/1023.0;

  // incluindo o valor no array de stat
  stat.addData(val);

  // ponto central (max_lido + min_lido / 2)
  float middle = (stat.maxVal() + stat.minVal()) / 2.0;
  // calculo media
  float mean_val = stat.mean();

  // se media menor que o ponto central, apitar alarme
 float limite = middle * 1;
  if(mean_val <= limite){
    triggerAlarm();
  }

  // exibindo no serial monitor
  Serial.println("A " + String (val));
  Serial.println("L " + String (limite));
  Serial.println("M " + String (mean_val) + "\n----");

//  Serial.println(val);
  // enviando para smartphone
  blth.print(val);
  blth.print("\n");
  delay(100);
}

void triggerAlarm(){
  int noteDuration = 1000 / NOTE_FRACTION;
  tone(13, NOTE ,noteDuration);
}
