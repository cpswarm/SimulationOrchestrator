#include <math.h>

class Result{
public:
  float output[2];
  Result(float outp[], long outputsize){
    long i;
	for (i=0L;i<outputsize; i=i+1){
	  output[i]=outp[i];
	}
  }
};


float bias[10]={0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.466151f, -1.1014818f, 0.6353644f, -0.59853417f};

float randombias[10]={0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};

float weight[10][10]={{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.66950566f, -0.74167156f, -0.14712438f, 1.6397536f}, {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.020178981f, -0.33069503f, -1.7242522f, 2.151824f}, {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.8441844f, 1.8340924f, -2.1145492f, -0.31562823f}, {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.95279825f, 0.43676752f, -0.12421353f, -0.590175f}, {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.7697216f, -0.89535415f, 2.800353f, 0.79424834f}, {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.6360753f, 0.07061845f, -1.7455866f, 1.4430494f}, {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.6485489f, 0.75010395f, 1.1459128f, -3.2082243f}, {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.075784475f, 0.22907004f, 1.3674892f, 2.2720928f}, {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.73032117f, 2.2531395f, -2.5111334f, -1.0180467f}, {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.5729234f, 1.2544858f, 1.113151f, 1.9288743f}};

float netOutput[10];

long long int seed=12345LL;

float sigmoidActivate(float x) {
	float y=(float)(1.0f / (1.0f + exp(-x)));
	return y;
}

float linearActivate(float x){
	if (x >= 1)
		{return 1;}
	else {
		if (x <= 0)
			{return 0;}
		else
			{return x;}
		}
}

Result getStep(float netInput[], long inputsize){
    long i;
	long j;
	float activation [10];
	for (i=0L; i < 10L; i=i+1){
		activation[i]=0.0f;
	}
    for (i=0L; i < inputsize; i=i+1) {
      netOutput[i]=netInput[i];
    }
	for (i=6L; i < 10L; i=i+1) {
      float sumValue=0.0f;
	  for (j=0L; j < 10L; j=j+1) {
        sumValue=sumValue+weight[j][i]*netOutput[j];
      }
	  activation[i]=bias[i]+sumValue;
    }
	float outputVector [2];
	for (i = 6L; i < 10L; i=i+1) {
      netOutput[i]=linearActivate(activation[i]);
    }
	for (i = (10L - 2L); i < 10L; i=i+1) {
	  j = i - (10L - 2L);
      outputVector[j]=netOutput[i];
    }
	Result r(outputVector,2L);
	return r;
}

Result getOutput(float netInput[],long inputsize){
  long i;
  for (i=0L; i < 10L; i=i+1) {
    netOutput[i]=0;
  }
  for (i=0L; i < 2L - 1; i=i+1) {
    getStep(netInput,inputsize);
  }
  return getStep(netInput,inputsize);
}
