echo "pw is a AND"
curl -v -X POST -u "*:*" --data-binary @"target/Rote-1.0.war" https://rote.scm.azurewebsites.net/api/wardeploy
echo "waiting to deploy ..."
sleep 40
curl rote.azurewebsites.net

