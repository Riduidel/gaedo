I must confess something : I hate bugs. Really. And I even more hate gaedo bugs : they are the living proof I'm not a perfect being. And this bug (issue: Bouncing queries do not work) I have a specific hatred for. Let me explain it with a simple example (borrowed from gaedo-blueprints - more on that later - test) : 


        @Test
        public void searchPostByAuthor() {
                int postsOf  = postService.find().matching(new QueryBuilder<PostInformer>() {

                        @Override
                        public QueryExpression createMatchingExpression(PostInformer informer) {
                                return informer.getAuthor().getLogin().equalsTo(USER_LOGIN);
                        }

                }).count();
                // All posts are from the same author
                assertThat(postsOf, Is.is(3));
        }


You see that request obtaining all posts from a user ? Well, it fails. Why ? Because we're looking up the Posts using a field from the User used as author. Unfortunatly, up to now, FieldInformer (which is the base interface upon which all the query is built) had no information regarding the "properties path" used to access it from the query root. I don't know if I'm perfectly clear here, and I would like to clarify that claim using the attached schema. 


 So, you see, starting from any Post (say as an example com.dooapp.gaedo.test.Post:java.lang.Long:1 - that's a hell of an id, isn't it ?), looking up the user login require us to go the following route : 

Post -> User -> login 

Unfortunatly, QueryExpression had, up to now, no knowledge of that route, making that query totally impossible. That's fortunatly no more the case, thanks to the addition of FieldInformerAPI, which is a derivation of fieldInformer all field informers (including Informer instances) should implement - don't worry, all those classes are gaedo one : your code won't change in any fashion. This implies small architecture changes, but nothing you should be aware of. And now, it will be possible to perform that query.