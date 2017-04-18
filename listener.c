#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <xdo.h>
#define MYPORT "4950"    // the port users will be connecting to

#define MAXBUFLEN 32

// get sockaddr, IPv4 or IPv6:
void *get_in_addr(struct sockaddr *sa)
{
    if (sa->sa_family == AF_INET) {
        return &(((struct sockaddr_in*)sa)->sin_addr);
    }

    return &(((struct sockaddr_in6*)sa)->sin6_addr);
}

int main(void)
{
    int sockfd;
    struct addrinfo hints, *servinfo, *p;
    int rv;
    int numbytes;
    struct sockaddr_storage their_addr;
    char buf[MAXBUFLEN];
    socklen_t addr_len;
    char s[INET6_ADDRSTRLEN];
    xdo_t *xdo = xdo_new(NULL);
    int isMouseDown=0;

    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC; // set to AF_INET to force IPv4
    hints.ai_socktype = SOCK_DGRAM;
    hints.ai_flags = AI_PASSIVE; // use my IP

    if ((rv = getaddrinfo(NULL, MYPORT, &hints, &servinfo)) != 0) {
        fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(rv));
        return 1;
    }

    // loop through all the results and bind to the first we can
    for(p = servinfo; p != NULL; p = p->ai_next) {
        if ((sockfd = socket(p->ai_family, p->ai_socktype,
                p->ai_protocol)) == -1) {
            perror("listener: socket");
            continue;
        }

        if (bind(sockfd, p->ai_addr, p->ai_addrlen) == -1) {
            close(sockfd);
            perror("listener: bind");
            continue;
        }

        break;
    }

    if (p == NULL) {
        fprintf(stderr, "listener: failed to bind socket\n");
        return 2;
    }

    freeaddrinfo(servinfo);
    printf("listener: waiting to recvfrom...\n");

    addr_len = sizeof their_addr;
    int i = 0;
    buf[0] = '\0';
    while(strcmp(buf,"quit\n")){
    if ((numbytes = recvfrom(sockfd, buf, MAXBUFLEN-1 , 0,(struct sockaddr *)&their_addr, &addr_len)) == -1) {
        perror("recvfrom");
        exit(1);
    }
    buf[numbytes] = '\0';
    char *token;
    char *search = ",";
    int dx,dy;

    printf("%s\n",buf);
    token = strtok(buf, search);
    if(!strcmp(token,"move")){
	    token = strtok(NULL, search);
	    dx=(int)atoi(token);
	    token = strtok(NULL, search);
	    dy=(int)atoi(token);
	    xdo_move_mouse_relative(xdo,dx,dy);
    }
    if(!strcmp(token,"scroll")){
	    token = strtok(NULL, search);
	    dy=(int)atof(token);
	    if(dy>0)
    	    	xdo_click_window(xdo,CURRENTWINDOW,4);
	    else
    	    	xdo_click_window(xdo,CURRENTWINDOW,5);
    }

    else if(!strcmp(token,"left_click"))
    	xdo_click_window(xdo,CURRENTWINDOW,1);
    else if(!strcmp(token,"right_click"))
    	xdo_click_window(xdo,CURRENTWINDOW,3);
    else if(!strcmp(token,"middle_click"))
    	xdo_click_window(xdo,CURRENTWINDOW,2);
    else if(!strcmp(token,"double_click"))
	xdo_click_window_multiple(xdo,CURRENTWINDOW,1,2,100);
    else if(!strcmp(token,"long_press")){
	    if(isMouseDown == 0){
		xdo_mouse_down(xdo,CURRENTWINDOW,1);
		isMouseDown = 1;
	    }
    	    else {	
		xdo_mouse_up(xdo,CURRENTWINDOW,1);
		isMouseDown = 0;
	    }
    }
    else if(!strcmp(token,"key")) {
	token = strtok(NULL, search);
	if(!strcmp(token,"undo")){
		char ch[] = "Control_L+z";
		xdo_send_keysequence_window(xdo,CURRENTWINDOW,ch,10);
		continue;
	}
	else if(!strcmp(token,"copy")){
		char ch[] = "Control_L+c";
		xdo_send_keysequence_window(xdo,CURRENTWINDOW,ch,10);
		continue;
	}
	else if(!strcmp(token,"paste")) {
		char ch[] = "Control_L+v";
		xdo_send_keysequence_window(xdo,CURRENTWINDOW,ch,10);
		continue;
	}
	int ascii = atoi(token);
	if(ascii == 10) {
		char ch[] = "Return";
		xdo_send_keysequence_window(xdo,CURRENTWINDOW,ch,10);
		continue;
	}
	else if(ascii == 8) {
		char ch[] = "BackSpace";
		xdo_send_keysequence_window(xdo,CURRENTWINDOW,ch,10);
		continue;
	}
	else if(ascii >= 32 && ascii <= 127 ) {
		char ch[2];
		ch[0] = ascii;
		ch[1] = '\0';
		xdo_send_keysequence_window(xdo,CURRENTWINDOW,ch,10);
	}
    }
/*
    i=0;
    while(buf[i]!='\0'){
	switch(buf[i]){
		case 'd':
			xdo_move_mouse_relative(xdo,2,0);
			break;
		case 'a':
			xdo_move_mouse_relative(xdo,-2,0);
			break;
		case 'w':
			xdo_move_mouse_relative(xdo,0,-2);
			break;
		case 's':
			xdo_move_mouse_relative(xdo,0,2);
			break;
		case 'c':
			xdo_click_window(xdo,CURRENTWINDOW,1);
			break;
		case 'r':
			xdo_click_window(xdo,CURRENTWINDOW,2);
			break;
		default:
			break;
	}
	i++;
	usleep(15000);
    }
    */
    /*
    printf("listener: got packet from %s\n", inet_ntop(their_addr.ss_family,get_in_addr((struct sockaddr *)&their_addr),s, sizeof s));
    printf("listener: packet is %d bytes long\n", numbytes);
    buf[numbytes] = '\0';
    printf("listener: packet contains \"%s\"\n", buf);
    */

    }
    close(sockfd);

    return 0;
}
