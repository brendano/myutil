import os,sys,re,json
import numpy as np

def pageheader():
    print """
    <meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
    <style>
    .depedge { font-size: 8pt; color: #333; }
    .wordinfo { font-size: 9pt; }
    .pos { color: blue; }
    .neg { color: red; }
    .score a { color: inherit; }
    </style>
    <link rel="stylesheet" href="http://brenocon.com/js/tablesorter/2.7.2/css/mytheme.css" >
    
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js"></script> 
    <script type="text/javascript" src="http://brenocon.com/js/tablesorter/2.7.2/js/jquery.tablesorter.min.js"></script> 
    """
    # <script type="text/javascript" src="http://brenocon.com/js/tablesorter/2.7.2/js/jquery.tablesorter.widgets.js"></script> 
    print """<script>
    $(document).ready(function() 
        { 
            $("table").tablesorter();
        } 
    ); 
    </script>
    """

prefix = sys.argv[1]
def fname(suff): return prefix + "." + suff

wc_thresh = 10

word_topics = np.loadtxt(fname("nWordTopic"))
Nword = word_topics.sum(1)
topiccounts = np.loadtxt(fname("nTopic"))
Ntopic = word_topics.shape[1]

word_counts = word_topics.sum(1)

word_vocab = os.path.join(os.path.dirname(prefix), "word.vocab")
word_vocab = np.array([x.strip() for x in open(word_vocab)])

# how about first PC as default instead...
topicorder = (-topiccounts).argsort()

pageheader()

print "<b>" + prefix + "</b>"
print "only showing paths with count >=", wc_thresh

print "<table class=tablesorter cellpadding=3 border=1 cellspacing=0 width='100%'>"

thead = ['k','count','top words']
print "<thead>", ' '.join(["<th>"+x for x in thead]), "</thead>"
print "<tbody>"
print

for k in topicorder:
    # top_words = (-word_topics[:,k]).argsort()
    top_words = (-word_topics[:,k]*1.0/word_counts).argsort()
    # top_words = (-word_topics[:,k]/Nword).argsort()
    top_words = top_words[ word_topics[top_words,k] >= wc_thresh]
    top_words = top_words[:30]

    # pathelts = [nicepath(x) for x in word_vocab[top_words]]
    # print json.dumps(pathelts[:9])
    # continue

    pathelts = ["%s <span class=wordinfo>(%.0f)</span>" % (word_vocab[i], word_topics[i,k]) for i in top_words]
    pathinfo = ',&nbsp; '.join(pathelts)
    row = ['k=%s' % k, str(topiccounts[k])]
    row += [pathinfo]
    print '<tr>' + ' '.join('<td>'+str(x) for x in row)

print "</tbody>"
print "</table>"


