** Due to Oracle drivers not being available via Maven Central, Oracle users will need to provide and include their own copy of
   the oracle jdbc driver. 
   
This utility will provide a simple way to either change the existing kalturaIds (KALTURA_ITEM.KALTURAID) with new values. 
 -- OR --
generate the SQL update statements to be run at a later time.

* Build the project using Maven (ie: mvn clean install)
* In the built target directory, configure the util.xml file:
  ** Un-comment out the first bean definition if you wish to create a file with the SQL statements. Set the name/path of the file you wish to generate.
    -- OR --
  ** Un-comment the second block of definitions and configure your database settings in util.xml
  
* If using Oracle: manually copy the JDBC driver for Oracle to the target/lib directory
* Execute the runme.sh or runme.bat file

FILE FORMAT:
The file containing the ids should be formatted as a comma separated value file. Each line should contain a pair of values as:

newid,oldid

Lines may be commented out using "--" (no quotes) to start the line. Note that there shouldn't be any whitespace following 
the comma separating the ids (unless the whitespace is part of the id).