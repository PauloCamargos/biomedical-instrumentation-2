#include "SoftwareSerial.h"
#include "Statistics.h"
//https://github.com/provideyourown/statistics
#include "pitches.h"
#include "TimerOne.h"

// PARAMETROS CONFIGURAVEIS
#define FA 10               // frequencia de amostragem [hz]
#define TEMPO_CALIBRACAO 10  // t para calibracao

// CONSTANTES
#define DELIMITADOR '$'
#define START 'S'
#define END 'E'
#define QNT_AMOSTRAS 10        // qnt amostras no vetor

// PARAMETROS DO PROGRAMA
#define TX 11             // pino tx (blth)
#define RX 12             // pino rx (blth)
#define ANALOG_PIN A7     // pino leitura do tecido
#define SOUND_PIN 13      // ouput buzzer
#define NOTE_FRACTION 20  // fracao de segundo do som
#define NOTE NOTE_A7      // nota musical do buzzer
#define BAUD_RATE 9600  // baud rate

// instanciando pino tx e rx do bluetooth
SoftwareSerial blth(RX, TX);

// instanciando Statistics com 10 valores
Statistics breathSignal(QNT_AMOSTRAS);
Statistics calibrationSignal(10.0 * FA);

unsigned long time_now = 0;
unsigned long time_to_alarm = 0;

int val;
float meanVal;
float maxVal;
float minVal;
float stdDev;
float stdAlarm = 15;        // desvio padrao limite para alarme
int tempoAlarme = 5;          // tempo para disparo de alarme [s]
float limiarStd = 0;
int fireAmount = 0;
bool fireWarned = false;

unsigned long TA = (1.0 / FA) * 1000000.0; // tempo de amostragem em us

char lixo;
String data = "";
int calibrationCounter = 0;

void setup() {
  //inciando comunicacao no serial monitor e bluetooth
  Serial.begin(BAUD_RATE);
  blth.begin(9600);
  // iniciando estatistica (stat)
  breathSignal.reset();
  calibrationSignal.reset();
  
}

void loop() {
  String inChar;
  if (blth.available()) {
    inChar = blth.readString();
    while (blth.available() > 0) {
      lixo = blth.read();
    }
    
    Serial.println(inChar.charAt(0) == START);
    if (inChar.charAt(0) == START) {
      calibrationCounter = 0;
      String expansao = "";
      String tempo = "";

      String expansaoTensao = inChar;
      int i = 1;
      char charPos = 'X';

      while (charPos != DELIMITADOR) {
        charPos = expansaoTensao.charAt(i);
        if (charPos == DELIMITADOR)
          break;
        expansao += charPos;
        i++;
      }
      i++;
      while (charPos != END) {
        charPos = expansaoTensao.charAt(i);
        if (charPos == END)
          break;
        tempo += charPos;
        i++;
      }

      tempoAlarme = tempo.toInt();
      limiarStd = expansao.toInt();

      Serial.println("tempoAlarm: " + (String) tempoAlarme);
      Serial.println("limiarStd: " + (String) limiarStd);

      Timer1.initialize(TA);
      Timer1.attachInterrupt(sendSignal);
      Serial.println("S");
    } else if (inChar.charAt(0) == END) {
      Timer1.detachInterrupt();
      Serial.println("E: " + (String) fireAmount );
    }

  }
}

void sendSignal() {
  maxVal = breathSignal.maxVal();
  minVal = breathSignal.minVal();

  val = analogRead(ANALOG_PIN);

  // incluindo o valor no array de stat
  breathSignal.addData(val);

  // media e desvio padrao
  meanVal = breathSignal.mean();
  stdDev = breathSignal.stdDeviation();

  /* PERIODO DE CALIBRACAO -----------------------------
    Colhe o desvio padrao durante os 10s iniciais
    da coleta. Realiza a media destes valores e
    multiplica pelo limiar definido no app.
     */
  if (calibrationCounter <= 100) {
    calibrationCounter++;
    calibrationSignal.addData(stdDev);
    stdAlarm = (limiarStd / 100.0) * calibrationSignal.mean();
  }
  /* ---------------------------------------------------*/

  byte mappedMean = map(meanVal, minVal, maxVal, 0, 255);

  /* DECISAO PARA DISPARAR ALARME  ---------------------*/
  if (stdDev <= stdAlarm) {
    if (millis() > (time_to_alarm + tempoAlarme * 1000)) {
      triggerAlarm();
      fireAmount++;
    }
  }
  else {
    time_to_alarm = millis();
    fireAmount = 0;
    fireWarned = false;
  }

  if(fireAmount != 0 && !fireWarned){
    blth.print('T');
    blth.print("\n");
    fireWarned = true;
  }
  /* ---------------------------------------------------*/

  /* IMPRESSAO DOS VALORES NO SERIAL MONITOR ------------*/
//  Serial.print(0);  // To freeze the lower limit
//  Serial.print("|");
//  Serial.print(255);  // To freeze the upper limit
//
//  Serial.print("|");
//  Serial.print(mappedMean);
//  Serial.print("|");
//  Serial.print(stdDev);
//  Serial.print("|");
//  Serial.print(stdAlarm);
//  Serial.print("|");
//  Serial.println(tempoAlarme);
  /* -----------------------------------------------*/

  /* DADOS PARA O ANDROID --------------------------*/
  blth.print(mappedMean);
  blth.print("\n");
  /* -----------------------------------------------*/
}

void triggerAlarm() {
  int noteDuration = 1000 / NOTE_FRACTION;
  tone(13, NOTE , noteDuration);
}
