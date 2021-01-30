#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

/* ******************************************************************
 ALTERNATING BIT AND GO-BACK-N NETWORK EMULATOR: SLIGHTLY MODIFIED
 FROM VERSION 1.1 of J.F.Kurose

   This code should be used for PA2, unidirectional or bidirectional
   data transfer protocols (from A to B. Bidirectional transfer of data
   is for extra credit and is not required).  Network properties:
   - one way network delay averages five time units (longer if there
       are other messages in the channel for GBN), but can be larger
   - packets can be corrupted (either the header or the data portion)
       or lost, according to user-defined probabilities
   - packets will be delivered in the order in which they were sent
       (although some can be lost).
**********************************************************************/

#define BIDIRECTIONAL 1 /* change to 1 if you're doing extra credit */
/* and write a routine called B_output */

#define LEN 4

/* a "pkt" is the data unit passed from layer 3 (teachers code) to layer  */
/* 2 (students' code).  It contains the data (characters) to be delivered */
/* to layer 3 via the students transport level protocol entities.         */
struct pkt
{
    char data[LEN];
};

/* a frame is the data unit passed from layer 2 (students code) to layer */
/* 1 (teachers code).  Note the pre-defined frame structure, which all   */
/* students must follow. */
struct frm
{
    int frametype;
    int seqnum;
    int acknum;
    int checksum;
    char payload[LEN];
};

/********* FUNCTION PROTOTYPES. DEFINED IN THE LATER PART******************/
void starttimer(int AorB, float increment);
void stoptimer(int AorB);
void tolayer1(int AorB, struct frm frame);
void tolayer3(int AorB, char datasent[LEN]);

/********* STUDENTS WRITE THE NEXT SEVEN ROUTINES *********/

int CRC_step=0;
int piggybacking=0;
char pol[10];
int outstanding_ack[2];
int seq_num[2];
int ack_num[2];
bool not_acked[2];
float inc_time[2];
struct frm frm_from_A;
struct frm frm_from_B;

void dec_to_bin(int x, char * arr)
{
    //char bits[33];

    int i,c,k;

    for(c=31,i=0;c>=0;i++,c--)
    {
        k=x>>c;

        if(k & 1)
        {
            arr[i]='1';
        }
        else arr[i]='0';

    }
    arr[32]='\0';
}

int bin_to_dec(char *bits)
{
    int c,k,n=0;

    for(c=31;c>=0;c--)
    {
        if(bits[c]=='1')
        {
            int k=31-c-1;

            if(k<0) n+=1;

            else n+=(2<<k);
        }
    }
    return n;
}
void char_to_bin(char x, char * arr)
{
    int i,c,k;

    for(c=7,i=0;c>=0;i++,c--)
    {
        k=x>>c;

        if(k & 1)
        {
            arr[i]='1';
        }
        else arr[i]='0';
    }
    arr[8]='\0';
}

void str_to_bin(char * str, char *res)
{
    //char res[33];
    res[0]=0;

    int i;
    for(i=0;i<4;i++)
    {
        char a[9];
        char_to_bin(str[i],a);
        strcat(res,a);
        //printf("%s\n",a);
    }
    //printf("%s\n",res);
}

int calculate_checksum(struct frm frame)
{
    if(CRC_step==1)
    {
        printf("Generator Polynomial : %s\n",pol);
    }
    int l=strlen(pol);
    int n=128+l;
    char res[n],a[33];
    res[0]=0;
    //printf("\ntype%d\n",frame.frametype);
    dec_to_bin(frame.frametype,a);
    strcat(res,a);
    //printf("\n%s\n",res);
    dec_to_bin(frame.seqnum,a);
    strcat(res,a);
    dec_to_bin(frame.acknum,a);
    strcat(res,a);
    str_to_bin(frame.payload,a);
    strcat(res,a);
    //printf("\n%s\n",res);

    int i;
    for(i=0;i<l-1;i++)
    {
        strcat(res,"0");
    }
    //printf("\n%s\n",res);
    char temp[n];
    strcpy(temp,res);
    //printf("\n%s\n",temp);

    if(CRC_step==1)
    {
        printf("Input Bit_String:\n");
        printf("%s\n",temp);
    }

    for(i=0;i<n-1;i++)
    {
        if(temp[i]=='1')
        {
            if(i+l-1>=n-1)
            {
                break;
            }
            int j;
            for(j=0;j<l;j++)
            {
                temp[i+j]=(temp[i+j]^ pol[j])+'0';
            }
            //if(CRC_step==1) printf("%s\n",temp);
        }
    }
    //printf("\n%s\n",temp);
    char bits[33];
    //bits[0]=0;
    strncpy(bits,temp+(n-33),33);
    //printf("%s",bits);

    int checksum=bin_to_dec(bits);
    //printf("\n%d",checksum);

    if(CRC_step==1)
    {
        printf("Remainder : %d\n",checksum);
    }
    return checksum;
}

void timerinterrupt(int AorB)
{
    char* func_name= AorB ? "B_timerinterrupt" : "A_timerinterrupt" ;
    char* msg= AorB ? frm_from_B.payload : frm_from_A.payload;
    struct frm f= AorB ? frm_from_B : frm_from_A;

    if(!not_acked[AorB])
    {
        printf("%s : Resending not needed.\n",func_name);
        return;
    }
    printf("%s : Resending frame's msg %s\n",func_name,msg);

    tolayer1(AorB,f);

    starttimer(AorB,inc_time[AorB]);
}

void output(int AorB, struct pkt packet)
{
    char* func_name= AorB ? "B_output" : "A_output" ;

    if(not_acked[AorB]){
        printf("%s : Last frame's not acknowledged yet. Can't send new frame. Drop frame's msg %s\n ",func_name,packet.data);
        return;
    }

    struct frm f;
    if(piggybacking==1 && outstanding_ack[AorB]==1)
    {
        printf("%s : Sending frame(data+ack): %s\n",func_name,packet.data);
        f.frametype=2;
        f.acknum=1-ack_num[1-AorB];

    }
    else
    {
        printf("%s : Sending frame(data): %s\n",func_name,packet.data);
        f.frametype=0;
        f.acknum=0;
    }

    if(CRC_step==1) printf("SENT FRAME:\n");
    f.seqnum=seq_num[AorB];
    strcpy(f.payload, packet.data);
    f.checksum=calculate_checksum(f);
    //printf("output--\n%d\n",f.checksum);

    if(AorB) frm_from_B=f;
    else frm_from_A=f;

    not_acked[AorB]=true;

    tolayer1(AorB,f);
    outstanding_ack[AorB]=0;
    starttimer(AorB,inc_time[AorB]);

}

/* called from layer 3, passed the data to be sent to other side */
void A_output(struct pkt packet)
{
    output(0,packet);
}
/* need be completed only for extra credit */
void B_output(struct pkt packet)
{
    output(1,packet);
}

void input(int AorB, struct frm frame)
{
    char* func_name= AorB ? "B_input" : "A_input" ;
    bool flag=false;

    if(CRC_step==1) printf("RECEIVED FRAME:\n");

    if(frame.frametype==0)
    {
        int get_checksum=calculate_checksum(frame);
        //printf("input---\n%d\n",get_checksum);
        if(frame.checksum!=get_checksum)
        {
            printf("%s : This frame(data) was corrupted, msg %s.\n",func_name,frame.payload);
            flag=true;
        }
        else if(ack_num[1-AorB]!=frame.seqnum)
        {
            printf("%s : Wrong(duplicate) data frame found.\n",func_name);
            flag=true;
        }
        struct frm f;
        f.frametype=1;
        f.seqnum=0;
        strcpy(f.payload,"GOT");

        if(flag)
        {
            if(CRC_step==1) printf("NACK FRAME:\n");
            f.acknum=1-ack_num[1-AorB];

            f.checksum=calculate_checksum(f);
            outstanding_ack[AorB]=0;
            tolayer1(AorB,f);

            return;
        }

        printf("%s : Received data frame: %s\n",func_name,frame.payload);
        tolayer3(AorB,frame.payload);
        outstanding_ack[AorB]=1;
        ack_num[1-AorB]=1-ack_num[1-AorB];

    }
    else if(piggybacking==1 && frame.frametype==2)
    {
        int get_checksum=calculate_checksum(frame);
        if(frame.checksum!=get_checksum)
        {
            printf("%s : This frame(data+ack) was corrupted, msg %s.\n",func_name,frame.payload);
            flag=true;
        }
        else if(ack_num[1-AorB]!=frame.seqnum)
        {
            printf("%s : Wrong(duplicate) frame found(data+ack).\n",func_name);
            flag=true;
        }
        struct frm f;
        f.frametype=1;
        f.seqnum=0;
        strcpy(f.payload,"GOT");

        if(flag)
        {
            if(CRC_step==1)  printf("NACK FRAME:\n");
            f.acknum=1-ack_num[1-AorB];

            f.checksum=calculate_checksum(f);
            outstanding_ack[AorB]=0;
            tolayer1(AorB,f);
            return;
        }

        if(seq_num[AorB]!=frame.acknum){
            printf("%s : Wrong ack frame found(data+ack). Drop frame\n",func_name);
            return;
        }

        printf("%s : Frame acknowledged(ack)\n",func_name);
        stoptimer(AorB);
        seq_num[AorB]=1-seq_num[AorB];
        not_acked[AorB]=false;

        printf("%s : Received data frame(data+ack): %s.\n",func_name,frame.payload);
        tolayer3(AorB,frame.payload);
        outstanding_ack[AorB]=1;
        ack_num[1-AorB]=1-ack_num[1-AorB];

    }
    else if(frame.frametype==1)
    {
        int get_checksum=calculate_checksum(frame);
        if(get_checksum!=frame.checksum){

            printf("%s : This ack frame was corrupted, msg %s. Drop frame\n",frame.payload);
            return;
        }
        if(seq_num[AorB]!=frame.acknum){
            printf("%s : Wrong ack frame found. Drop frame\n",func_name);
            return;
        }
        printf("%s : Frame acknowledged(ack)\n",func_name,frame.payload);
        stoptimer(AorB);

        seq_num[AorB]=1-seq_num[AorB];
        not_acked[AorB]=false;
        tolayer3(AorB,frame.payload);
    }
}

/* called from layer 1, when a frame arrives for layer 2 */
void A_input(struct frm frame)
{
    input(0,frame);
}
/* Note that with simplex transfer from a-to-B, there is no B_output() */

/* called from layer 1, when a frame arrives for layer 2 at B*/
void B_input(struct frm frame)
{
    input(1,frame);
}
/* called when A's timer goes off */
void A_timerinterrupt(void)
{
    timerinterrupt(0);
}
/* called when B's timer goes off */
void B_timerinterrupt(void)
{
    timerinterrupt(1);
}

/* the following routine will be called once (only) before any other */
/* entity A routines are called. You can use it to do any initialization */
void A_init(void)
{
    not_acked[0]=false;
    inc_time[0]=50;
    seq_num[0]=0;
    ack_num[1]=0;
}

/* the following rouytine will be called once (only) before any other */
/* entity B routines are called. You can use it to do any initialization */
void B_init(void)
{
    not_acked[1]=false;
    inc_time[1]=15;
    ack_num[0]=0;
    seq_num[1]=0;
}

/*****************************************************************
***************** NETWORK EMULATION CODE STARTS BELOW ***********
The code below emulates the layer 1 and below network environment:
    - emulates the transmission and delivery (possibly with bit-level corruption
        and frame loss) of packets across the layer 1/2 interface
    - handles the starting/stopping of a timer, and generates timer
        interrupts (resulting in calling students timer handler).
    - generates packet to be sent (passed from later 3 to 2)

THERE IS NOT REASON THAT ANY STUDENT SHOULD HAVE TO READ OR UNDERSTAND
THE CODE BELOW.  YOU SHOLD NOT TOUCH, OR REFERENCE (in your code) ANY
OF THE DATA STRUCTURES BELOW.  If you're interested in how I designed
the emulator, you're welcome to look at the code - but again, you should have
to, and you definitely should not have to modify
******************************************************************/

struct event
{
    float evtime;       /* event time */
    int evtype;         /* event type code */
    int eventity;       /* entity where event occurs */
    struct frm *frmptr; /* ptr to frame (if any) assoc w/ this event */
    struct event *prev;
    struct event *next;
};
struct event *evlist = NULL; /* the event list */

/* possible events: */
#define TIMER_INTERRUPT 0
#define FROM_LAYER3 1
#define FROM_LAYER1 2

#define OFF 0
#define ON 1
#define A 0
#define B 1

int TRACE = 1;     /* for my debugging */
int nsim = 0;      /* number of messages from 3 to 2 so far */
int nsimmax = 0;   /* number of pkts to generate, then stop */
float time = 0.000;
float lossprob;    /* probability that a frame is dropped  */
float corruptprob; /* probability that one bit is frame is flipped */
float lambda;      /* arrival rate of messages from layer 3 */
int ntolayer1;     /* number sent into layer 1 */
int nlost;         /* number lost in media */
int ncorrupt;      /* number corrupted by media*/

void init();
void generate_next_arrival(void);
void insertevent(struct event *p);

int main()
{
    struct event *eventptr;
    struct pkt pkt2give;
    struct frm frm2give;

    int i, j;
    char c;

    init();
    A_init();
    B_init();

    while (1)
    {
        eventptr = evlist; /* get next event to simulate */
        if (eventptr == NULL)
            goto terminate;
        evlist = evlist->next; /* remove this event from event list */
        if (evlist != NULL)
            evlist->prev = NULL;
        if (TRACE >= 2)
        {
            printf("\nEVENT time: %f,", eventptr->evtime);
            printf("  type: %d", eventptr->evtype);
            if (eventptr->evtype == 0)
                printf(", timerinterrupt  ");
            else if (eventptr->evtype == 1)
                printf(", fromlayer3 ");
            else
                printf(", fromlayer1 ");
            printf(" entity: %d\n", eventptr->eventity);
        }
        time = eventptr->evtime; /* update time to next event time */
        if (eventptr->evtype == FROM_LAYER3)
        {
            if (nsim < nsimmax)
            {
                if (nsim + 1 < nsimmax)
                    generate_next_arrival(); /* set up future arrival */
                /* fill in pkt to give with string of same letter */
                j = nsim % 26;
                for (i = 0; i < LEN; i++)
                    pkt2give.data[i] = 97 + j;
                pkt2give.data[LEN - 1] = 0;
                if (TRACE > 2)
                {
                    printf("          MAINLOOP: data given to student: ");
                    for (i = 0; i < LEN; i++)
                        printf("%c", pkt2give.data[i]);
                    printf("\n");
                }
                nsim++;
                if (eventptr->eventity == A)
                    A_output(pkt2give);
                else
                    B_output(pkt2give);
            }
        }
        else if (eventptr->evtype == FROM_LAYER1)
        {
            frm2give.frametype = eventptr->frmptr->frametype;
            frm2give.seqnum = eventptr->frmptr->seqnum;
            frm2give.acknum = eventptr->frmptr->acknum;
            frm2give.checksum = eventptr->frmptr->checksum;
            for (i = 0; i < LEN; i++)
                frm2give.payload[i] = eventptr->frmptr->payload[i];
            if (eventptr->eventity == A) /* deliver frame by calling */
                A_input(frm2give); /* appropriate entity */
            else
                B_input(frm2give);
            free(eventptr->frmptr); /* free the memory for frame */
        }
        else if (eventptr->evtype == TIMER_INTERRUPT)
        {
            if (eventptr->eventity == A)
                A_timerinterrupt();
            else
                B_timerinterrupt();
        }
        else
        {
            printf("INTERNAL PANIC: unknown event type \n");
        }
        free(eventptr);
    }

    terminate:
    printf(
            " Simulator terminated at time %f\n after sending %d pkts from layer3\n",
            time, nsim);
}

void init() /* initialize the simulator */
{
    int i;
    float sum, avg;
    float jimsrand();

    printf("-----  Stop and Wait Network Simulator Version 1.1 -------- \n\n");
    printf("Enter the number of messages to simulate: ");
    scanf("%d",&nsimmax);
    printf("Enter  frame loss probability [enter 0.0 for no loss]:");
    scanf("%f",&lossprob);
    printf("Enter frame corruption probability [0.0 for no corruption]:");
    scanf("%f",&corruptprob);
    printf("Enter average time between messages from sender's layer3 [ > 0.0]:");
    scanf("%f",&lambda);
    printf("Show CRC steps [0 for not showing]:");
    scanf("%d",&CRC_step);
    printf("Enable piggybacking [0 for disabling]:");
    scanf("%d",&piggybacking);
    printf("Enter generator polynomial [Like 1011]:");
    scanf("%s",pol);
    printf("Enter TRACE:");
    scanf("%d",&TRACE);

    srand(9999); /* init random number generator */
    sum = 0.0;   /* test random number generator for students */
    for (i = 0; i < 1000; i++)
        sum = sum + jimsrand(); /* jimsrand() should be uniform in [0,1] */
    avg = sum / 1000.0;
    if (avg < 0.25 || avg > 0.75)
    {
        printf("It is likely that random number generation on your machine\n");
        printf("is different from what this emulator expects.  Please take\n");
        printf("a look at the routine jimsrand() in the emulator code. Sorry. \n");
        exit(1);
    }

    ntolayer1 = 0;
    nlost = 0;
    ncorrupt = 0;

    time = 0.0;              /* initialize time to 0.0 */
    generate_next_arrival(); /* initialize event list */
}

/****************************************************************************/
/* jimsrand(): return a float in range [0,1].  The routine below is used to */
/* isolate all random number generation in one location.  We assume that the*/
/* system-supplied rand() function return an int in therange [0,mmm]        */
/****************************************************************************/
float jimsrand(void)
{
    double mmm = RAND_MAX;
    float x;                 /* individual students may need to change mmm */
    x = rand() / mmm;        /* x should be uniform in [0,1] */
    return (x);
}

/********************* EVENT HANDLINE ROUTINES *******/
/*  The next set of routines handle the event list   */
/*****************************************************/

void generate_next_arrival(void)
{
    double x, log(), ceil();
    struct event *evptr;
    float ttime;
    int tempint;

    if (TRACE > 2)
        printf("          GENERATE NEXT ARRIVAL: creating new arrival\n");

    x = lambda * jimsrand() * 2; /* x is uniform on [0,2*lambda] */
    /* having mean of lambda        */
    evptr = (struct event *)malloc(sizeof(struct event));
    evptr->evtime = time + x;
    evptr->evtype = FROM_LAYER3;
    if (BIDIRECTIONAL && (jimsrand() > 0.5))
        evptr->eventity = B;
    else
        evptr->eventity = A;
    insertevent(evptr);
}

void insertevent(struct event *p)
{
    struct event *q, *qold;

    if (TRACE > 2)
    {
        printf("            INSERTEVENT: time is %lf\n", time);
        printf("            INSERTEVENT: future time will be %lf\n", p->evtime);
    }
    q = evlist;      /* q points to header of list in which p struct inserted */
    if (q == NULL)   /* list is empty */
    {
        evlist = p;
        p->next = NULL;
        p->prev = NULL;
    }
    else
    {
        for (qold = q; q != NULL && p->evtime > q->evtime; q = q->next)
            qold = q;
        if (q == NULL)   /* end of list */
        {
            qold->next = p;
            p->prev = qold;
            p->next = NULL;
        }
        else if (q == evlist)     /* front of list */
        {
            p->next = evlist;
            p->prev = NULL;
            p->next->prev = p;
            evlist = p;
        }
        else     /* middle of list */
        {
            p->next = q;
            p->prev = q->prev;
            q->prev->next = p;
            q->prev = p;
        }
    }
}

void printevlist(void)
{
    struct event *q;
    int i;
    printf("--------------\nEvent List Follows:\n");
    for (q = evlist; q != NULL; q = q->next)
    {
        printf("Event time: %f, type: %d entity: %d\n", q->evtime, q->evtype,
               q->eventity);
    }
    printf("--------------\n");
}

/********************** Student-callable ROUTINES ***********************/

/* called by students routine to cancel a previously-started timer */
void stoptimer(int AorB /* A or B is trying to stop timer */)
{
    struct event *q, *qold;

    if (TRACE > 2)
        printf("          STOP TIMER: stopping timer at %f\n", time);
    /* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next)  */
    for (q = evlist; q != NULL; q = q->next)
        if ((q->evtype == TIMER_INTERRUPT && q->eventity == AorB))
        {
            /* remove this event */
            if (q->next == NULL && q->prev == NULL)
                evlist = NULL;          /* remove first and only event on list */
            else if (q->next == NULL) /* end of list - there is one in front */
                q->prev->next = NULL;
            else if (q == evlist)   /* front of list - there must be event after */
            {
                q->next->prev = NULL;
                evlist = q->next;
            }
            else     /* middle of list */
            {
                q->next->prev = q->prev;
                q->prev->next = q->next;
            }
            free(q);
            return;
        }
    printf("Warning: unable to cancel your timer. It wasn't running.\n");
}

void starttimer(int AorB /* A or B is trying to start timer */, float increment)
{
    struct event *q;
    struct event *evptr;

    if (TRACE > 2)
        printf("          START TIMER: starting timer at %f\n", time);
    /* be nice: check to see if timer is already started, if so, then  warn */
    /* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next)  */
    for (q = evlist; q != NULL; q = q->next)
        if ((q->evtype == TIMER_INTERRUPT && q->eventity == AorB))
        {
            printf("Warning: attempt to start a timer that is already started\n");
            return;
        }

    /* create future event for when timer goes off */
    evptr = (struct event *)malloc(sizeof(struct event));
    evptr->evtime = time + increment;
    evptr->evtype = TIMER_INTERRUPT;
    evptr->eventity = AorB;
    insertevent(evptr);
}

/************************** TOLAYER1 ***************/
void tolayer1(int AorB, struct frm frame)
{
    struct frm *myfrmptr;
    struct event *evptr, *q;
    float lastime, x;
    int i;

    ntolayer1++;

    /* simulate losses: */
    if (jimsrand() < lossprob)
    {
        nlost++;
        if (TRACE > 0)
            printf("          TOLAYER1: frame being lost\n");
        return;
    }

    /* make a copy of the frame student just gave me since he/she may decide */
    /* to do something with the frame after we return back to him/her */
    myfrmptr = (struct frm *)malloc(sizeof(struct frm));
    myfrmptr->frametype = frame.frametype;
    myfrmptr->seqnum = frame.seqnum;
    myfrmptr->acknum = frame.acknum;
    myfrmptr->checksum = frame.checksum;
    for (i = 0; i < LEN; i++)
        myfrmptr->payload[i] = frame.payload[i];
    if (TRACE > 2)
    {
        printf("          TOLAYER1: seq: %d, ack %d, check: %d ", myfrmptr->seqnum,
               myfrmptr->acknum, myfrmptr->checksum);
        for (i = 0; i < LEN; i++)
            printf("%c", myfrmptr->payload[i]);
        printf("\n");
    }

    /* create future event for arrival of frame at the other side */
    evptr = (struct event *)malloc(sizeof(struct event));
    evptr->evtype = FROM_LAYER1;      /* frame will pop out from layer1 */
    evptr->eventity = (AorB + 1) % 2; /* event occurs at other entity */
    evptr->frmptr = myfrmptr;         /* save ptr to my copy of frame */
    /* finally, compute the arrival time of frame at the other end.
       medium can not reorder, so make sure frame arrives between 1 and 10
       time units after the latest arrival time of packets
       currently in the medium on their way to the destination */
    lastime = time;
    /* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next) */
    for (q = evlist; q != NULL; q = q->next)
        if ((q->evtype == FROM_LAYER1 && q->eventity == evptr->eventity))
            lastime = q->evtime;
    evptr->evtime = lastime + 1 + 9 * jimsrand();

    /* simulate corruption: */
    if (jimsrand() < corruptprob)
    {
        ncorrupt++;
        if ((x = jimsrand()) < .75)
            myfrmptr->payload[0] = 'Z'; /* corrupt payload */
        else if (x < .875)
            myfrmptr->seqnum = 999999;
        else
            myfrmptr->acknum = 999999;
        if (TRACE > 0)
            printf("          TOLAYER1: frame being corrupted\n");
    }

    if (TRACE > 2)
        printf("          TOLAYER1: scheduling arrival on other side\n");
    insertevent(evptr);
}

void tolayer3(int AorB, char datasent[LEN])
{
    int i;
    if (TRACE > 2)
    {
        printf("          TOLAYER3: data received: ");
        for (i = 0; i < LEN; i++)
            printf("%c", datasent[i]);
        printf("\n");
    }
}


