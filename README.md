# TopStoriesForHackerNews
An app that pulls the top stories from Hacker News and stores them remotely in a SQLite Database. After downloading the data online, the title of the articles and their associated webpages can be viewed offline. AsyncTask and Retrofit are both implemented for execution time comparisons.

<h3>Features:</h3>
<ul>
  <li>Hacker News API <a href="https://github.com/HackerNews/API">https://github.com/HackerNews/API</a></li>
  <li>Parsing JSON objects with <a href="http://square.github.io/retrofit/">Retrofit</a></li>
  <li>Parsing raw HTML data with Retrofit</li>
  <li>Using AsyncTask to make Synchronous RF calls</li>
  <li>Permanant data storage with SQLiteDatabase</li>
  <li>Handler class using SQLiteOpenHelper</li>
  <li>Parsing JSON with AsyncTask</li>
  <li>Displays execution time for comparisons between the clients</li>
  <li>Viewing webpages</li>
  <li>Interactive ListView</li>
</ul>
