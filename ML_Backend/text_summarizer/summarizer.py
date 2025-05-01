from transformers import pipeline

class TextSummarizer:
    def __init__(self):
        pass
    #     self.model = "sshleifer/distilbart-cnn-12-6"  # smaller and faster model
    #     self.summarizer = pipeline("summarization", model=self.model)

    def summarize_text(self, text):
        return "This is a summary"
    #     summary = self.summarizer(text, max_length=130, min_length=30, do_sample=False)
    #     return summary[0]['summary_text']


    # def summarize(self, text):
    #     return "This is a summary"




# # Load the summarization pipeline
# from transformers import pipeline

# class TextSummarizer:
    
    
#     def summarize(self, text):
#         summary = self.summarizer(text, max_length=130, min_length=30, do_sample=False)
#         return summary[0]['summary_text']
