FROM node:latest

WORKDIR /app

ADD start.sh /root/start.sh
RUN npm install nodemon -g

ADD ./package.json /data/package.json
RUN cd /data && npm install
ENV NODE_PATH=/data/node_modules

ADD . /app

ENTRYPOINT /root/start.sh
CMD ["server.js"]
