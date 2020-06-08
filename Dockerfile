FROM node:10
# app directory
WORKDIR /usr/src/app
# Copy neccessary files
COPY ./functions ./functions
COPY ./functions ./functions
COPY ./firebase.json ./
COPY ./.firebaserc ./
COPY ./.firebase ./
WORKDIR /usr/src/app/functions
RUN npm i --only=production -g pm2@latest firebase-tools cross-env typescript
RUN npm ci --only=production
CMD ["pm2-runtime", "start", "daemon.json"]
